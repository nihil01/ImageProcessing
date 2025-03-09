package world.horosho.prictureprocessor.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.List;

import world.horosho.prictureprocessor.R;

public class UIModifier {
    public boolean generatedFlag = false;
    private ConstraintLayout cl;
    private final Context ctx;
    private final Bitmap image;
    private final ImageInterface listener;
    private List<View> dynamicViews = new ArrayList<>();

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
}