package world.horosho.prictureprocessor;

import android.app.Activity;
import android.content.Intent;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.util.Log;

import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;


import androidx.activity.EdgeToEdge;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
    }

    public void requestPictureActivity(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        resultLauncher.launch(photoPickerIntent);
    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();

                try {
                    if (data != null) {
                        Uri imageUri = data.getData();
                        if (imageUri != null) {
                            // Get the parent layout that contains the current ImageView

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
                            siv.setImageURI(imageUri);

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
                            gen.resolveImageData(imageUri, projection, findViewById(R.id.imageData));
                        }
                    }
                } catch (Exception e) {
                    Log.e("imageErr", "Error setting image: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });


    //interface methods
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
