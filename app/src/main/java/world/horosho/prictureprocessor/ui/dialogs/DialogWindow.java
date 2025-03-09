package world.horosho.prictureprocessor.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;


import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import world.horosho.prictureprocessor.R;
import world.horosho.prictureprocessor.imageProcessing.executor.AsyncExecutor;
import world.horosho.prictureprocessor.imageProcessing.engine.ImageProcessor;

public class DialogWindow {

    private final Context ctx;
    private final ImageProcessor ip;
    private final AsyncExecutor ae;

    public DialogWindow(Context ctx, ImageProcessor ip, AsyncExecutor ae){
        this.ctx = ctx;
        this.ip = ip;
        this.ae = ae;
    }

    public void build(String title, int layout, DialogTypes type){
        AlertDialog.Builder ab = new AlertDialog.Builder(this.ctx)
                .setTitle(title)
                .setCancelable(true)
                .setView(layout);

        AlertDialog ad = ab.create();

        ad.show();

        switch (type){

            case GAMMA: {
                generateGamma(ad);
                break;
            }

            case FILTER_COLOR_CHANNEL: {
                generateFilterChannel(ad);
                break;
            }

            case SEPIA_TONING: {
                generateSepia(ad);
                break;
            }

            case DECREASE_COLOR_DEPTH: {
                decreaseColorDepth(ad);
                break;
            }

            case CONTRAST:{
                contrast(ad);
                break;
            }

            case BRIGHTNESS:{
                brightness(ad);
                break;
            }

            case SHARPNESS:{
                sharpen(ad);
                break;
            }

            case COLOR_BOOST: {
                boost(ad);
                break;
            }
        }

    }

    public void generateGamma(AlertDialog ad){

        Button btn = ad.findViewById(R.id.button);
        btn.setOnClickListener(click -> {

            String redGamma = ((EditText)ad.findViewById(R.id.red_gamma)).getText().toString();
            String greenGamma = ((EditText)ad.findViewById(R.id.green_gamma)).getText().toString();
            String blueGamma = ((EditText)ad.findViewById(R.id.blue_gamma)).getText().toString();

            if (validate(redGamma) && validate(greenGamma)
                    && validate(blueGamma)){

                ae.exec(() -> ip.doGamma((int)Double.parseDouble(redGamma),
                        (int)Double.parseDouble(greenGamma),(int)Double.parseDouble(blueGamma)));

            }else{
                Toast.makeText(ctx, "Invalid input values!", Toast.LENGTH_SHORT).show();

            }

        });

    }

    public void generateFilterChannel(AlertDialog ad){

        Button btn = ad.findViewById(R.id.button_filter);
        btn.setOnClickListener(click -> {

            String redFilter = ((EditText)ad.findViewById(R.id.red_filter)).getText().toString();
            String greenFilter = ((EditText)ad.findViewById(R.id.green_filter)).getText().toString();
            String blueFilter = ((EditText)ad.findViewById(R.id.blue_filter)).getText().toString();

            if (validate(redFilter) && validate(greenFilter)
                    && validate(blueFilter)){

                ae.exec(() -> ip.filterColor((int)Double.parseDouble(redFilter), (int)Double.parseDouble(greenFilter),(int)Double.parseDouble(blueFilter)));

            }else{
                Toast.makeText(ctx, "Invalid input values!", Toast.LENGTH_SHORT).show();

            }

        });

    }

    public void generateSepia(AlertDialog ad){

        Button btn = ad.findViewById(R.id.button_sepia);
        btn.setOnClickListener(click -> {

            String redVal = ((EditText)ad.findViewById(R.id.red_sepia)).getText().toString();
            String greenVal = ((EditText)ad.findViewById(R.id.green_sepia)).getText().toString();
            String blueVal = ((EditText)ad.findViewById(R.id.blue_sepia)).getText().toString();
            String depth = ((EditText)ad.findViewById(R.id.depth_sepia)).getText().toString();


            if (validate(redVal) && validate(greenVal)
                    && validate(blueVal) && Integer.parseInt(depth) >= 0){

                ae.exec(() -> ip.sepiaToning((int) Double.parseDouble(depth), Double.parseDouble(redVal),
                        Double.parseDouble(greenVal),Double.parseDouble(blueVal)));

            }else{
                Toast.makeText(ctx, "Invalid input values!", Toast.LENGTH_SHORT).show();

            }

        });

    }

    public void decreaseColorDepth(AlertDialog ad){

            Button btn = ad.findViewById(R.id.button_color_depth);
            btn.setOnClickListener(click -> {

                String depth = ((EditText)ad.findViewById(R.id.color_depth)).getText().toString();


                if (validate(depth)){

                    ae.exec(() ->
                            ip.decreaseColorDepth((int) Double.parseDouble(depth))
                    );

                }else{
                    Toast.makeText(ctx, "Invalid input values!", Toast.LENGTH_SHORT).show();

                }

            });
    }

    public void contrast(AlertDialog ad){

        Button btn = ad.findViewById(R.id.button_contrast);
        btn.setOnClickListener(click -> {

            String contrast = ((EditText)ad.findViewById(R.id.contrast)).getText().toString();


            if (validate(contrast)){

                ae.exec(() ->
                        ip.createContrast((int) Double.parseDouble(contrast))
                );

            }else{
                Toast.makeText(ctx, "Invalid input values!", Toast.LENGTH_SHORT).show();

            }

        });

    }

    public void brightness(AlertDialog ad){

        Button btn = ad.findViewById(R.id.button_brightness);
        btn.setOnClickListener(click -> {

            String brightness = ((EditText)ad.findViewById(R.id.brightness)).getText().toString();


            if (validate(brightness)){

                ae.exec(() ->
                        ip.bright((int) Double.parseDouble(brightness))
                );

            }else{
                Toast.makeText(ctx, "Invalid input values!", Toast.LENGTH_SHORT).show();

            }

        });

    }


    public void sharpen(AlertDialog ad){
        Button btn = ad.findViewById(R.id.button_sharpness);
        btn.setOnClickListener(click -> {

            String sharpness = ((EditText)ad.findViewById(R.id.sharpness)).getText().toString();


            if (validate(sharpness)){

                ae.exec(() ->
                        ip.sharpen((int) Double.parseDouble(sharpness))
                );

            }else{
                Toast.makeText(ctx, "Invalid input values!", Toast.LENGTH_SHORT).show();

            }

        });
    }

    public void boost(AlertDialog ad){
        Button btn  = ad.findViewById(R.id.button_color_boost);

        btn.setOnClickListener(click -> {
            String type = ((EditText)ad.findViewById(R.id.typeValue)).getText().toString();
            SeekBar seekBar = ad.findViewById(R.id.seekBar);


            if (validate(type)){
                int val = (int) Double.parseDouble(type);

                if (val == 1){
                    ae.exec(() -> ip.boost(seekBar.getProgress(), val));

                }else if (val == 2){
                    ae.exec(() -> ip.boost(seekBar.getProgress(), val));

                }else if (val == 3){
                    ae.exec(() -> ip.boost(seekBar.getProgress(), val));

                }else{
                    Toast.makeText(ctx, "Invalid type! Choose in range of 1 - 3 !", Toast.LENGTH_SHORT).show();
                }

            }

        });


    }


    private boolean validate(String text){
        try {

            return !text.isEmpty() && Double.parseDouble(text) > 0 && Double.parseDouble(text) < 256;

        } catch (NumberFormatException e) {

            Log.e("dialogWindow", e.getMessage());
            return false;

        }

    }

}
