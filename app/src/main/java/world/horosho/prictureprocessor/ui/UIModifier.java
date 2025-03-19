package world.horosho.prictureprocessor.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import world.horosho.prictureprocessor.R;
import world.horosho.prictureprocessor.http.Endpoints;
import world.horosho.prictureprocessor.http.ImageFetchCallback;

public class UIModifier {
    public boolean generatedFlag = false;
    private ConstraintLayout cl;
    private final Context ctx;
    private final Bitmap image;
    private final ImageInterface listener;
    private final List<View> dynamicViews = new ArrayList<>();
    private Timer timer;

    public UIModifier(Context ctx, Bitmap image, ImageInterface ii){
        this.ctx = ctx;
        this.image = image;
        this.listener = ii;
    }


    public void modifyForImageRotation() {
        if (ctx == null || image == null || generatedFlag) {
            Log.d("ImageInfo", "smth is null");
            return;
        }

        if (!dynamicViews.isEmpty()) destroy();

        try {
            cl = ((Activity) ctx).findViewById(R.id.main_constraint);

            // Create Left Rotation Button
            Button leftRotation = new Button(ctx);
            leftRotation.setText("Left");
            leftRotation.setId(View.generateViewId()); // Assign an ID for referencing
            ConstraintLayout.LayoutParams leftParams = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);

            // Set constraints for Left Button
            leftParams.bottomToTop = R.id.generateBtn; // Position above generateBtn
            leftParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            leftParams.setMargins(40, 100, 0, 40); // Margin for spacing
            leftRotation.setLayoutParams(leftParams);

            // Create Right Rotation Button
            Button rightRotation = new Button(ctx);
            rightRotation.setText("Right");
            rightRotation.setId(View.generateViewId()); // Assign an ID for referencing
            ConstraintLayout.LayoutParams rightParams = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);

            // Set constraints for Right Button (same row as Left Button)
            rightParams.bottomToTop = R.id.generateBtn; // Align with Left Button vertically
            rightParams.startToEnd = leftRotation.getId(); // Position to the right of Left Button
            rightParams.setMargins(40, 100, 0, 40); // Margin for spacing
            rightRotation.setLayoutParams(rightParams);

            // Create Degree Count EditText
            EditText degreeCount = new EditText(ctx);
            degreeCount.setTextColor(Color.BLACK);
            degreeCount.setBackgroundColor(Color.GRAY);
            degreeCount.setHint("Degree count");
            degreeCount.setInputType(InputType.TYPE_CLASS_NUMBER);
            degreeCount.setId(View.generateViewId()); // Assign an ID for referencing
            ConstraintLayout.LayoutParams degreeParams = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);

            // Set constraints for Degree Count EditText
            degreeParams.bottomToTop = R.id.generateBtn; // Position above Left Button
            degreeParams.startToEnd = rightRotation.getId();
            degreeParams.setMargins(40, 100, 0, 40); // Margin for spacing
            degreeCount.setLayoutParams(degreeParams);

            //listeners
            leftRotation.setOnClickListener(l -> {
                if (degreeCount.getText() != null && !degreeCount.getText().toString().isEmpty()){
                    rotate(-1, (int) Double.parseDouble(degreeCount.getText().toString()));
                }
            });

            rightRotation.setOnClickListener(l -> {
                if (degreeCount.getText() != null && !degreeCount.getText().toString().isEmpty()){
                    rotate(1, (int) Double.parseDouble(degreeCount.getText().toString()));
                }
            });

            // Add views to the ConstraintLayout
            addViewToLayout(leftRotation);
            addViewToLayout(rightRotation);
            addViewToLayout(degreeCount);

            generatedFlag = true;
        } catch (Exception e) {
            Log.e("imageItem", e.getMessage());
        }
    }

    public void modifyForImageMirroring(){
        try {

            if (ctx == null || image == null || generatedFlag) {
                Log.d("ImageInfo", "smth is null");
                return;
            }

            if (!dynamicViews.isEmpty()) destroy();

            cl = ((Activity) ctx).findViewById(R.id.main_constraint);

            Button mirrorH = new Button(ctx);
            mirrorH.setText("Horizontal");
            mirrorH.setId(View.generateViewId());
            ConstraintLayout.LayoutParams hParams = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
            hParams.bottomToTop = R.id.generateBtn; // Position above generateBtn
            hParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            hParams.setMargins(40, 100, 0, 40); // Margin for spacing
            mirrorH.setLayoutParams(hParams);


            Button mirrorV = new Button(ctx);
            mirrorV.setText("Vertical");
            mirrorV.setId(View.generateViewId());

            ConstraintLayout.LayoutParams vParams = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
            vParams.bottomToTop = R.id.generateBtn; // Position above generateBtn
            vParams.startToEnd = mirrorH.getId();
            vParams.setMargins(40, 100, 0, 40); // Margin for spacing
            mirrorV.setLayoutParams(vParams);

            mirrorV.setOnClickListener(l -> mirror(1));
            mirrorH.setOnClickListener(l -> mirror(-1));

            addViewToLayout(mirrorH);
            addViewToLayout(mirrorV);

            generatedFlag = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void modifyForImageUrlInput(){
        try {

            if (!dynamicViews.isEmpty()) destroy();

            cl = ((Activity) ctx).findViewById(R.id.main_constraint);


            EditText editText = new EditText(ctx);
            editText.setTextSize(16);
            editText.setHint("Type URL here ...");
            editText.setId(View.generateViewId());
            editText.setTextColor(Color.BLACK);
            editText.setBackgroundColor(Color.GRAY);

            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT
            );

            params.bottomToTop = R.id.generateBtn;
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;

            editText.setLayoutParams(params);

            final String[] textData = new String[1];
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                   if (timer != null){
                       timer.cancel();
                   }

                   textData[0] = charSequence.toString();
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {

                            if (textData[0].contains("http") && isValidURI(textData[0])){
                                Log.d("urlCheck", "URL IS VALID!");
                                Endpoints.fetchImageByURL(textData[0], new ImageFetchCallback() {
                                    @Override
                                    public void onSuccess(Bitmap bmp) {
                                        if (bmp != null){
                                            new Handler(Looper.getMainLooper()).post(() -> {
                                                listener.updateImage(bmp);
                                                Toast.makeText(ctx, "Image updated successfully", Toast.LENGTH_SHORT).show();

                                            });
                                        }else{
                                            Log.d("Fetch image", "image is null1!1");
                                        }
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        Toast.makeText(ctx, error, Toast.LENGTH_SHORT).show();
                                    }
                                });


                            }
                        }
                    }, 2000);
                }
            });

            addViewToLayout(editText);

            generatedFlag = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void addViewToLayout(View view) {
        cl.addView(view);
        dynamicViews.add(view);
    }

    private void rotate(int direction, int degrees){
        try {
            Matrix matrix = new Matrix();
            matrix.postRotate(direction * degrees);
            listener.updateImage(Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true));
            listener.modifySaveBtn(true);
        } catch (Exception e) {
            Log.e("ImageItem", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void mirror(int direction){
        try {
            Matrix matrix = new Matrix();
            if (direction == 1){
               matrix.preScale(1.0f, -1.0f);
            }else{
                matrix.preScale(-1.0f, 1.0f);
            }

            listener.updateImage(Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true));
            listener.modifySaveBtn(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void destroy(){
        if (generatedFlag && cl != null){

            for (View view: dynamicViews){
                cl.removeView(view);
            }

            dynamicViews.clear();
            generatedFlag = false;
        }
    }

    private boolean isValidURI(String url){
        try {
            URL u = new URL(url); // this would check for the protocol
            u.toURI();
            Log.d("Fetch image", "incoming url is " + url);
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            Toast.makeText(ctx, "Invalid URL specified!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}