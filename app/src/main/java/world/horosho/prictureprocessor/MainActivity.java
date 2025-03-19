package world.horosho.prictureprocessor;

import static android.icu.number.NumberRangeFormatter.with;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.provider.MediaStore;
import android.util.Log;

import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkManagerInitializer;
import androidx.work.WorkRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import world.horosho.prictureprocessor.notifications.NotificationService;
import world.horosho.prictureprocessor.ui.ImageInterface;
import world.horosho.prictureprocessor.ui.MainUIProvider;
import world.horosho.prictureprocessor.ui.ScalableImageView;

public class MainActivity extends AppCompatActivity implements ImageInterface {
    private MainUIProvider gen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //app functionalities

        gen = new MainUIProvider(this, this);

        //initial variables
        Spinner spinner = findViewById(R.id.dropdown);
        Button imagePicker = findViewById(R.id.imagePick);
        Button genBtn = findViewById(R.id.generateBtn);

        if (spinner != null){
            gen.fillSpinnerElementWithValues(spinner);
        }

        if (imagePicker != null){
            imagePicker.setOnClickListener(view ->requestPictureActivity());
        }

        if (genBtn != null){
            genBtn.setOnClickListener(e -> gen.processImageFilter());
        }

        //initial check for notifications
        if (!NotificationService.checkNotificationPermission(this)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
        }

    }

    public void requestPictureActivity(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        resultLauncher.launch(photoPickerIntent);
    }

     ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                List<Uri> imageURIs = new ArrayList<>();

                try {
                    if (data != null && data.getClipData() != null) {

                        int count = data.getClipData().getItemCount();

                        if (count > 2) {
                            Toast.makeText(this, "Please, select 1 or 2 images ..", Toast.LENGTH_LONG).show();
                            return;
                        }

                        for (int i = 0; i < count; i++) {

                            Uri image = data.getClipData().getItemAt(i).getUri();
                            imageURIs.add(image);

                        }
                    }else if (data != null && data.getData() != null){

                        Uri image = data.getData();
                        imageURIs.add(image);

                    }

                    processImages(imageURIs);
                } catch (Exception e) {
                    Log.e("imageErr", "Error setting image: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });


    private void processImages(List<Uri> imageURIs){
        ImageView originalImageView = findViewById(R.id.imageView);
        ViewGroup parent = (ViewGroup) originalImageView.getParent();
        int index = parent.indexOfChild(originalImageView);

        // Create the new ScalableImageView
        ScalableImageView siv = new ScalableImageView(this);
        siv.setId(R.id.imageView); // Keep the same ID

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1080);


        siv.setPaddingRelative(
                50,50,50,50
        );

        siv.setLayoutParams(params);

        siv.setBackgroundResource(R.drawable.image_border);

        // Set the image URI
        siv.setImageURI(imageURIs.get(0));

        // Replace the original ImageView with ScalableImageView
        parent.removeView(originalImageView);
        parent.addView(siv, index);

        // Fetch and display image metadata
        String[] projection = {
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE
        };

        modifySaveBtn(false);
        gen.resolveImageData(imageURIs, projection, findViewById(R.id.imageData));
    }

    public void setWorkManager() {
        String workTag = "image_processor";
        WorkManager wi = WorkManager.getInstance(this);

        wi.getWorkInfosByTagLiveData(workTag).observeForever(workInfos -> {
            boolean exists = false;

            for (WorkInfo workInfo : workInfos) {
                if (workInfo.getState() == WorkInfo.State.ENQUEUED || workInfo.getState() == WorkInfo.State.RUNNING) {
                    exists = true;
                    break;
                }

            }

            if (!exists){
                WorkRequest workRequest = new PeriodicWorkRequest.Builder(NotificationService.class,
                        15, TimeUnit.MINUTES).addTag(workTag).build();
                WorkManager.getInstance(this).enqueue(workRequest);
            }
        });
    }


    //interface methods
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setWorkManager(); // Запуск воркера после получения разрешения
            } else {
                Log.e("Permissions", "Разрешение на уведомления отклонено");
            }
        }
    }
    @Override
    public void updateImage(Bitmap img) {
        ((ImageView)findViewById(R.id.imageView)).setImageBitmap(img);

        Button saveBtn = findViewById(R.id.saveBtn);

        if (saveBtn != null){
            saveBtn.setOnClickListener(v -> {
                gen.saveBitmapToFileStorage(this, img);
            });
        }

    }

    @Override
    public void modifySaveBtn(boolean state) {
        findViewById(R.id.saveBtn).setEnabled(state);
    }

    @Override
    public void modifyGenerateBtn(boolean state) {
        findViewById(R.id.generateBtn).setEnabled(state);
    }

}
