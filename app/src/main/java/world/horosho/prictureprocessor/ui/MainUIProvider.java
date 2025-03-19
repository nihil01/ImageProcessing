package world.horosho.prictureprocessor.ui;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import world.horosho.prictureprocessor.R;
import world.horosho.prictureprocessor.http.Endpoints;

import world.horosho.prictureprocessor.imageProcessing.executor.AsyncExecutor;
import world.horosho.prictureprocessor.imageProcessing.engine.ImageProcessor;
import world.horosho.prictureprocessor.ui.dialogs.DialogTypes;
import world.horosho.prictureprocessor.ui.dialogs.DialogWindow;

public class MainUIProvider extends AppCompatActivity {
    protected Context ctx;
    private Bitmap image;
    private Bitmap[] multipleImages;
    private Spinner imagePreset;
    private ImageInterface updateListener;
    private UIModifier um;

    public MainUIProvider(){
        //
    }

    public MainUIProvider(Context ctx, ImageInterface updateListener){
        this.ctx = ctx;
        this.updateListener = updateListener;
    }

    public void fillSpinnerElementWithValues(Spinner spinner){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            ctx,
            R.array.image_presets,
            android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setGravity(200);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                try {
                    if (um != null) {

                        if (adapterView.getSelectedItem().toString().equals("Rotate")) {
                            um.modifyForImageRotation();

                        }
                        else if (adapterView.getSelectedItem().toString().equals("Mirroring")){
                            um.modifyForImageMirroring();

                        }
                        else if(adapterView.getSelectedItem().toString().equals("Get image by URL")){
                            um.modifyForImageUrlInput();
                            updateListener.modifySaveBtn(true);
                        }
                        else{
                            um.destroy();
                        }

                    }
                } catch (Exception e) {
                    Log.e("Something", e.getMessage());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        imagePreset = spinner;
    }

    @SuppressLint("SetTextI18n")
    private void resolvePerImage(Uri imageUri, String[] proj, TextView imageData){
        try(Cursor cursor = ctx.getContentResolver().query(imageUri, proj, null, null,null)){
            if (cursor != null && cursor.moveToFirst()) {
                imageData.setText(getImageInfo(cursor) + getMetaDataInfo(imageUri));
            }
        } catch (Exception e) {
            Log.d("imageData", Objects.requireNonNull(e.getMessage()));
        }
    }

    public void resolveImageData(List<Uri> imageUri, String[] proj, TextView imageData) {

        if (imageUri.size() > 1){

            if (multipleImages == null || multipleImages.length < imageUri.size()) {
                multipleImages = new Bitmap[imageUri.size()];
            }

            for (int i = 0; i < imageUri.size(); i++) {

                //no need for image metadata with projection var
                    ContentResolver cr = ctx.getContentResolver();

                try {
                    InputStream is = cr.openInputStream(imageUri.get(i));

                    if (is != null){

                        multipleImages[i] = BitmapFactory.decodeStream(is);
                        is.close();

                    }
                } catch (IOException e) {
                    Log.e("NOT_FOUND", e.getMessage());
                }


            }

        }

        resolvePerImage(imageUri.get(0), proj, imageData);
    }

    protected void updateImageView(Bitmap bm){
        try{
            updateListener.updateImage(bm);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void saveBitmapToFileStorage(Context ctx, Bitmap bmp){

        try{

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy_MM_dd_HH_mm_ss");

            String dateTimeStr = LocalDateTime.now().format(formatter);

            String fileName = "ProcessedImage_"+dateTimeStr+".jpg";
            // Get the content resolver
            ContentResolver contentResolver = ctx.getContentResolver();

            // Create the MediaStore URI for the "Pictures" directory
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PictureProcessor");

            // Insert into MediaStore and get the URI of the new image
            Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            // Open an output stream to write the bitmap to the new file
            try (OutputStream outStream = contentResolver.openOutputStream(uri)) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ctx.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));

            Toast.makeText(ctx, "Image has been saved to:\n" + uri, Toast.LENGTH_LONG).show();

        } catch (RuntimeException e) {
            Log.e("wtf", e.getMessage());
        }
    }

    private String getImageInfo(Cursor cursor){
        int date = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
        int mime = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE);
        int size = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);

        ZonedDateTime cd = ZonedDateTime.ofInstant(Instant.ofEpochMilli(cursor.getLong(date)),
                ZoneId.of(TimeZone.getDefault().getID()));

        String imageSize = "";
        long sizeCalc = Long.parseLong(cursor.getString(size));
        if (sizeCalc / (1024 * 1024) > 0){
            imageSize = sizeCalc / (1024 * 1024) + "Mb";
        }else if ((sizeCalc / (1024) > 0)){
            imageSize = sizeCalc / (1024) + "Kb";
        }else{
            imageSize = "0 Kb";
        }

        int year = cd.getYear();
        String month = cd.getMonthValue() > 9 ? String.valueOf(cd.getMonthValue()) : "0"+cd.getMonthValue();
        int day = cd.getDayOfMonth();

        return "Saved: " + year+"."+month+"."+day+" "+(cd.getHour()+":"+cd.getMinute())+"; " + "MIME: " + cursor.getString(mime)
                + "; " + "SIZE: " + imageSize + "; ";
    }

    private String getMetaDataInfo(Uri imageUri) throws IOException {
        ContentResolver cr = ctx.getContentResolver();

        //streams should be opened separately, for BitmapFactory and ExifInterface

        try(
            InputStream bitmapStream = cr.openInputStream(imageUri);
            InputStream exifStream = cr.openInputStream(imageUri)
        ){

            if (bitmapStream == null || exifStream == null){
                return null;
            }

            //1
            image = BitmapFactory.decodeStream(bitmapStream);
            um = new UIModifier(ctx, image, updateListener);

            //2
            String model, datetime, manufacturer;

            ExifInterface exif = new ExifInterface(exifStream);
            model = exif.getAttribute(ExifInterface.TAG_MODEL);
            datetime = exif.getAttribute(ExifInterface.TAG_DATETIME);
            manufacturer = exif.getAttribute(ExifInterface.TAG_MAKE);

            return "Manufacturer: " +(manufacturer == null ? "Not Defined " : manufacturer)+
                    ";Camera Model: " +(model == null ? "Not Defined " : model) +
                    ";Photo taken: " + (datetime == null ? "Not Defined" : datetime);

        }

    }


    public void processImageFilter(){

        Object selectedSpinnerValue = imagePreset.getSelectedItem();

        if (selectedSpinnerValue == null || image == null) {
            Toast.makeText(ctx, "Selected item's value is empty or image object is omitted!", Toast.LENGTH_SHORT).show();
            return;
        };

        //value of dropdown item
        String itemVal = selectedSpinnerValue.toString();

        ImageProcessor ip = new ImageProcessor(image, multipleImages);
        AsyncExecutor ae = new AsyncExecutor(ctx, updateListener);
        DialogWindow dialogWindow = new DialogWindow(ctx, ip, ae);


        try {
            switch (itemVal) {
                //process image and save in byte array
                case "Highlight Image": {
                    updateImageView(ip.highlight());
                    break;
                }
                case "Invert Image": {
                    ae.exec(ip::invert);
                    break;
                }
                case "Grayscale": {
                    ae.exec(ip::grayScale);
                    break;
                }
                case "Gamma Correction": {
                    dialogWindow.build("Gamma Correction Dialog",
                            R.layout.dialog_window_gamma, DialogTypes.GAMMA);
                    break;
                }
                case "Filter Color Channel": {
                    dialogWindow.build("Filter Color Channel Dialog",
                            R.layout.dialog_window_filter_color_channel, DialogTypes.FILTER_COLOR_CHANNEL);
                    break;
                }
                case "Sepia Toning": {
                    dialogWindow.build("Sepia Toning Dialog",
                            R.layout.dialog_sepia_toning, DialogTypes.SEPIA_TONING);
                    break;
                }
                case "Decrease Color Depth": {
                    dialogWindow.build("Decrease Color Depth Dialog",
                            R.layout.dialog_decrease_color_depth, DialogTypes.DECREASE_COLOR_DEPTH);
                    break;
                }
                case "Contrast": {
                    dialogWindow.build("Contrast Dialog", R.layout.dialog_contrast,
                            DialogTypes.CONTRAST);
                    break;
                }
                case "Brightness": {
                    dialogWindow.build("Brightness Dialog", R.layout.dialog_brightness,
                            DialogTypes.BRIGHTNESS);
                    break;
                }
                case "Gaussian Blur": {
                    ae.exec(ip::gaussianBlur);
                    break;
                }
                case "Sharpness": {
                    dialogWindow.build("Sharpness Dialog", R.layout.dialog_sharpness,
                            DialogTypes.SHARPNESS);
                    break;
                }
                case "Mean Removal": {
                    ae.exec(ip::meanRemoval);
                    break;
                }
                case "Smoothness": {
                    ae.exec(ip::smoothEffect);
                    break;
                }
                case "Emboss": {
                    ae.exec(ip::emboss);
                    break;
                }
                case "Engrave": {
                    ae.exec(ip::engrave);
                    break;
                }
                case "Color Boost Up": {
                    dialogWindow.build("Color Boost Dialog", R.layout.dialog_color_boost,
                            DialogTypes.COLOR_BOOST);
                    break;
                }
                case "Noise": {
                    ae.exec(ip::makeNoise);
                    break;
                }
                case "Black Filter": {
                    ae.exec(ip::blackFilter);
                    break;
                }
                case "Hue Filter": {
                    ae.exec(ip::applyHueFilter);
                    break;
                }
                case "Reflection": {
                    ae.exec(ip::applyReflection);
                    break;
                }
                case "Saturation": {
                    ae.exec(ip::applySaturationFilter);
                    break;
                }
                case "Shading": {
                    ae.exec(ip::applyShadingFilter);
                    break;
                }
                case "Image Blending (2 images)": {
                    ip.sendDataToServerForProcessing("blend_images", updateListener);
                    break;
                }
                case "Histogram": {
                    ip.sendDataToServerForProcessing("histogram", updateListener);
                    break;
                }
                case "Threshold" : {
                    ip.sendDataToServerForProcessing("threshold", updateListener);
                    break;
                }
                case "Random Color Space": {
                    ip.sendDataToServerForProcessing("random_color_space", updateListener);
                    break;
                }
                case "Canny": {
                    ip.sendDataToServerForProcessing("canny", updateListener);
                    break;
                }
            }
        } catch (Exception e) {
            Log.e("imageError", e.getMessage());
            throw new RuntimeException(e);
        }

    }

}
