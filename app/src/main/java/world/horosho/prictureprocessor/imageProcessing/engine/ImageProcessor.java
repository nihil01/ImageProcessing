package world.horosho.prictureprocessor.imageProcessing.engine;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.Log;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import world.horosho.prictureprocessor.R;
import world.horosho.prictureprocessor.http.Endpoints;
import world.horosho.prictureprocessor.http.ImageFetchCallback;
import world.horosho.prictureprocessor.ui.ImageInterface;

public class ImageProcessor {
    private final Bitmap src;
    private Bitmap[] images;

    private final int COLOR_MAX = 0xff;
    private final int COLOR_MIN = 0x00;

    public ImageProcessor(Bitmap img, Bitmap[] multipleImages) {
        this.src = img;
        this.images = multipleImages;
    }


    //highlighting image
    public Bitmap highlight() {
        // create new bitmap, which will be painted and becomes result image
        Bitmap bmOut = Bitmap.createBitmap(src.getWidth() + 300, src.getHeight() + 200, Bitmap.Config.ARGB_8888);
//             setup canvas for painting
        Canvas canvas = new Canvas(bmOut);
//             setup default color
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
//
//             create a blur paint for capturing alpha
        Paint ptBlur = new Paint();
        ptBlur.setMaskFilter(new BlurMaskFilter(50, BlurMaskFilter.Blur.OUTER));
        int[] offsetXY = new int[2];
//            // capture alpha into a bitmap
        Bitmap bmAlpha = src.extractAlpha(ptBlur, offsetXY);
//            // create a color paint
        Paint ptAlphaColor = new Paint();
        ptAlphaColor.setColor(0xFFFFFFFF);
//            // paint color for captured alpha region (bitmap)
        canvas.drawBitmap(bmAlpha, offsetXY[0], offsetXY[1], ptAlphaColor);
//            // free memory
        bmAlpha.recycle();
//             paint the image source
        canvas.drawBitmap(src, 0, 0, null);
        // return out final image
        return addWatermark(bmOut);
    }

    public Bitmap invert() {

        int A, R, G, B;
        int colorPixel;

        Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());

        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {

                colorPixel = src.getPixel(x, y);
                A = Color.alpha(colorPixel);
                R = 255 - Color.red(colorPixel);
                G = 255 - Color.green(colorPixel);
                B = 255 - Color.blue(colorPixel);

                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        return addWatermark(bmOut);
    }

    public Bitmap grayScale() {
        //Formula for GrayScale

        final double GS_RED = 0.29;
        final double GS_GREEN = 0.58;
        final double GS_BLUE = 0.11;

        int A, R, G, B;
        int colorPixel;

        Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());

        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {

                colorPixel = src.getPixel(x, y);
                A = Color.alpha(colorPixel);
                R = Color.red(colorPixel);
                G = Color.green(colorPixel);
                B = Color.blue(colorPixel);

                R = G = B = (int) (GS_RED * R + GS_GREEN * G + GS_BLUE * B);

                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        return addWatermark(bmOut);
    }

    public Bitmap doGamma(int red, int green, int blue) {
        Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());

        int width = src.getWidth();
        int height = src.getHeight();

        int pixel;
        int A, R, G, B;

        // constant value curve
        final int MAX_SIZE = 256;
        final double MAX_VALUE_DBL = 255.0;
        final int MAX_VALUE_INT = 255;
        final double REVERSE = 1.0;

        // gamma arrays
        int[] gammaR = new int[MAX_SIZE];
        int[] gammaG = new int[MAX_SIZE];
        int[] gammaB = new int[MAX_SIZE];

        // setting values for every gamma channels
        for (int i = 0; i < MAX_SIZE; ++i) {
            gammaR[i] = Math.min(MAX_VALUE_INT,
                    (int) ((MAX_VALUE_DBL * Math.pow(i / MAX_VALUE_DBL, REVERSE / red)) + 0.5));

            gammaG[i] = Math.min(MAX_VALUE_INT,
                    (int) ((MAX_VALUE_DBL * Math.pow(i / MAX_VALUE_DBL, REVERSE / green)) + 0.5));

            gammaB[i] = Math.min(MAX_VALUE_INT,
                    (int) ((MAX_VALUE_DBL * Math.pow(i / MAX_VALUE_DBL, REVERSE / blue)) + 0.5));
        }

        // apply gamma table
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                // look up gamma
                R = gammaR[Color.red(pixel)];
                G = gammaG[Color.green(pixel)];
                B = gammaB[Color.blue(pixel)];
                // set new color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        return addWatermark(bmOut);
    }

    public Bitmap filterColor(int red, int green, int blue) {

        int pixelColor;
        int A, R, G, B;
        Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        int height = src.getHeight();
        int width = src.getWidth();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                pixelColor = src.getPixel(x, y);

                A = Color.alpha(pixelColor);
                R = Color.red(pixelColor) * red;
                G = Color.green(pixelColor) * green;
                B = Color.blue(pixelColor) * blue;

                bmOut.setPixel(x, y, Color.argb(A, R, G, B));

            }
        }

        return addWatermark(bmOut);
    }

    public Bitmap sepiaToning(int depth, double red, double green, double blue) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // constant grayscale
        final double GS_RED = 0.3;
        final double GS_GREEN = 0.59;
        final double GS_BLUE = 0.11;
        // color information
        int A, R, G, B;
        int pixel;

        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                // get color on each channel
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                // apply grayscale sample
                B = G = R = (int) (GS_RED * R + GS_GREEN * G + GS_BLUE * B);

                // apply intensity level for sepid-toning on each channel
                R += (depth * red);
                if (R > 255) {
                    R = 255;
                }

                G += (depth * green);
                if (G > 255) {
                    G = 255;
                }

                B += (depth * blue);
                if (B > 255) {
                    B = 255;
                }

                // set new pixel color to output image
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return addWatermark(bmOut);
    }

    public Bitmap decreaseColorDepth(int bitOffset) {
        // get image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;

        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {

                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                // round-off color offset
                R = ((R + (bitOffset / 2)) - ((R + (bitOffset / 2)) % bitOffset) - 1);
                if (R < 0) {
                    R = 0;
                }
                G = ((G + (bitOffset / 2)) - ((G + (bitOffset / 2)) % bitOffset) - 1);
                if (G < 0) {
                    G = 0;
                }
                B = ((B + (bitOffset / 2)) - ((B + (bitOffset / 2)) % bitOffset) - 1);
                if (B < 0) {
                    B = 0;
                }

                // set pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return addWatermark(bmOut);
    }

    public Bitmap createContrast(double value) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;
        // get contrast value
        double contrast = Math.pow((100 + value) / 100, 2);

        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                // apply filter contrast for every channel R, G, B
                R = Color.red(pixel);
                R = (int) (((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (R < 0) {
                    R = 0;
                } else if (R > 255) {
                    R = 255;
                }

                G = Color.red(pixel);
                G = (int) (((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (G < 0) {
                    G = 0;
                } else if (G > 255) {
                    G = 255;
                }

                B = Color.red(pixel);
                B = (int) (((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (B < 0) {
                    B = 0;
                } else if (B > 255) {
                    B = 255;
                }

                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return addWatermark(bmOut);
    }

    public Bitmap bright(int value) {
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;

        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                // increase/decrease each channel
                R += value;
                if (R > 255) {
                    R = 255;
                } else if (R < 0) {
                    R = 0;
                }

                G += value;
                if (G > 255) {
                    G = 255;
                } else if (G < 0) {
                    G = 0;
                }

                B += value;
                if (B > 255) {
                    B = 255;
                } else if (B < 0) {
                    B = 0;
                }

                // apply new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return addWatermark(bmOut);
    }

    public Bitmap addWatermark(Bitmap image) {
        Bitmap mutableBmp = image.copy(Bitmap.Config.ARGB_8888, true);

        int textSize;
        int height = image.getHeight();
        int width = image.getWidth();

        if (width > 500) {
            textSize = 50;

        } else if (width < 300) {
            textSize = 30;

        } else {
            textSize = 20;

        }

        String txt = "Image by Horosho World";
        Paint paint = new Paint();
        paint.setColor(Color.rgb(255, 0, 0));
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC));


        Canvas canvas = new Canvas(mutableBmp);
        canvas.drawText(txt, width - (txt.length() * 50), height - 100, paint);

        return mutableBmp;
    }


    public Bitmap gaussianBlur() {
        ConvolutionMatrix cm = new ConvolutionMatrix(3);
        cm.applyConfig(ConvolutionPresets.GAUSSIAN_BLUR.getValues());
        cm.Factor = 16;
        cm.Offset = 0;
        return addWatermark(ConvolutionMatrix.computeConvolution3x3(src, cm));
    }

    public Bitmap sharpen(double value) {
        ConvolutionMatrix cm = new ConvolutionMatrix(3);
        cm.applyConfig(ConvolutionPresets.modifyPresetWithValues(ConvolutionPresets.SHARPENING.getValues(), 1, 1, value));
        cm.Factor = value - 8;
        cm.Offset = 0;
        return addWatermark(ConvolutionMatrix.computeConvolution3x3(src, cm));
    }

    public Bitmap meanRemoval() {
        ConvolutionMatrix cm = new ConvolutionMatrix(3);
        cm.applyConfig(ConvolutionPresets.MEAN_REMOVAL.getValues());
        cm.Factor = 1;
        cm.Offset = 0;
        return addWatermark(ConvolutionMatrix.computeConvolution3x3(src, cm));
    }

    public Bitmap smoothEffect() {
        ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
        convMatrix.setAll(1);
        convMatrix.Matrix[1][1] = 5;
        convMatrix.Factor = 5 + 8;
        convMatrix.Offset = 1;
        return addWatermark(ConvolutionMatrix.computeConvolution3x3(src, convMatrix));
    }

    public Bitmap emboss() {
        ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
        convMatrix.applyConfig(ConvolutionPresets.EMBOSS.getValues());
        convMatrix.Factor = 1;
        convMatrix.Offset = 127;
        return addWatermark(ConvolutionMatrix.computeConvolution3x3(src, convMatrix));
    }

    public Bitmap engrave() {
        ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
        convMatrix.setAll(0);
        convMatrix.Matrix[0][0] = -2;
        convMatrix.Matrix[1][1] = 2;
        convMatrix.Factor = 1;
        convMatrix.Offset = 95;
        return addWatermark(ConvolutionMatrix.computeConvolution3x3(src, convMatrix));
    }

    public Bitmap boost(int value, int type) {

        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());

        int A, R, G, B, colorPixel;


        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                colorPixel = src.getPixel(x, y);
                A = Color.alpha(colorPixel);
                R = Color.red(colorPixel);
                G = Color.green(colorPixel);
                B = Color.blue(colorPixel);

                if (type == 1) {
                    R = R * (1 + value);
                    if (R > 255) R = 255;
                } else if (type == 2) {
                    G = G * (1 + value);
                    if (G > 255) G = 255;
                } else {
                    B = B * (1 + value);
                    if (B > 255) B = 255;
                }

                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        return addWatermark(bmOut);
    }

    public Bitmap makeNoise() {
        int width = src.getWidth();
        int height = src.getHeight();

        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());

        int[] pixels = new int[width * height];
        src.getPixels(pixels, 0, width, 0, 0, width, height);

        Random random = new Random();
        int index = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // get current index in 2D-matrix
                index = y * width + x;
                // get random color
                int randColor = Color.rgb(random.nextInt(COLOR_MAX),
                        random.nextInt(COLOR_MAX), random.nextInt(COLOR_MAX));
                // OR
                pixels[index] |= randColor;

            }
        }
        bmOut.setPixels(pixels, 0, width, 0, 0, width, height);
        return addWatermark(bmOut);
    }


    public Bitmap blackFilter() {
        // get image size
        int width = src.getWidth();
        int height = src.getHeight();
        int[] pixels = new int[width * height];
        // get pixel array from source
        src.getPixels(pixels, 0, width, 0, 0, width, height);
        // random object
        Random random = new Random();

        int R, G, B, index, thresHold;
        // iteration through pixels
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                // get current index in 2D-matrix
                index = y * width + x;
                // get color
                R = Color.red(pixels[index]);
                G = Color.green(pixels[index]);
                B = Color.blue(pixels[index]);
                // generate threshold
                thresHold = random.nextInt(COLOR_MAX);
                if (R < thresHold && G < thresHold && B < thresHold) {
                    pixels[index] = Color.rgb(COLOR_MIN, COLOR_MIN, COLOR_MIN);
                }
            }
        }
        // output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmOut.setPixels(pixels, 0, width, 0, 0, width, height);
        return addWatermark(bmOut);
    }

    public Bitmap applyHueFilter() {
        // get image size
        int width = src.getWidth();
        int height = src.getHeight();
        int[] pixels = new int[width * height];
        float[] HSV = new float[3];
        // get pixel array from source
        src.getPixels(pixels, 0, width, 0, 0, width, height);

        int index = 0;
        // iteration through pixels
        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                // get current index in 2D-matrix
                index = y * width + x;
                // convert to HSV
                Color.colorToHSV(pixels[index], HSV);
                // increase Saturation level
                HSV[0] *= new Random().nextInt(10);
                HSV[0] = (float) Math.max(0.0, Math.min(HSV[0], 360.0));
                // take color back
                pixels[index] |= Color.HSVToColor(HSV);
            }
        }
        // output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmOut.setPixels(pixels, 0, width, 0, 0, width, height);
        return addWatermark(bmOut);
    }


    public  Bitmap applyReflection() {
        // gap space between original and reflected
        final int reflectionGap = 4;
        // get image size
        int width = src.getWidth();
        int height = src.getHeight();

        // this will not scale but will flip on the Y axis
        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);

        // create a Bitmap with the flip matrix applied to it.
        // we only want the bottom half of the image
        Bitmap reflectionImage = Bitmap.createBitmap(src, 0, height/2, width, height/2, matrix, false);

        // create a new bitmap with same width but taller to fit reflection
        Bitmap bitmapWithReflection = Bitmap.createBitmap(width, (height + height/2), Bitmap.Config.ARGB_8888);

        // create a new Canvas with the bitmap that's big enough for
        // the image plus gap plus reflection
        Canvas canvas = new Canvas(bitmapWithReflection);
        // draw in the original image
        canvas.drawBitmap(src, 0, 0, null);
        // draw in the gap
        Paint defaultPaint = new Paint();
        canvas.drawRect(0, height, width, height + reflectionGap, defaultPaint);
        // draw in the reflection
        canvas.drawBitmap(reflectionImage,0, height + reflectionGap, null);

        // create a shader that is a linear gradient that covers the reflection
        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0, src.getHeight(), 0,
                bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff, 0x00ffffff,
                Shader.TileMode.CLAMP);

        // set the paint to use this shader (linear gradient)
        paint.setShader(shader);
        // set the Transfer mode to be porter duff and destination in
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        // draw a rectangle using the paint with our linear gradient
        canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + reflectionGap, paint);

        return addWatermark(bitmapWithReflection);
    }

    public Bitmap applySaturationFilter() {
        // get image size
        int width = src.getWidth();
        int height = src.getHeight();
        int[] pixels = new int[width * height];
        float[] HSV = new float[3];
        // get pixel array from source
        src.getPixels(pixels, 0, width, 0, 0, width, height);

        int index = 0;
        // iteration through pixels
        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                // get current index in 2D-matrix
                index = y * width + x;
                // convert to HSV
                Color.colorToHSV(pixels[index], HSV);
                // increase Saturation level
                HSV[1] *= new Random().nextInt(10);
                HSV[1] = (float) Math.max(0.0, Math.min(HSV[1], 1.0));
                // take color back
                pixels[index] |= Color.HSVToColor(HSV);
            }
        }
        // output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmOut.setPixels(pixels, 0, width, 0, 0, width, height);
        return addWatermark(bmOut);
    }

    public Bitmap applyShadingFilter() {
        // get image size
        int width = src.getWidth();
        int height = src.getHeight();
        int[] pixels = new int[width * height];
        Random random = new Random();
        // get pixel array from source
        src.getPixels(pixels, 0, width, 0, 0, width, height);

        int index = 0;
        // iteration through pixels
        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                // get current index in 2D-matrix
                index = y * width + x;
                // AND
                pixels[index] &= Color.rgb(random.nextInt(COLOR_MAX),
                        random.nextInt(COLOR_MAX), random.nextInt(COLOR_MAX));
            }
        }
        // output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmOut.setPixels(pixels, 0, width, 0, 0, width, height);
        return addWatermark(bmOut);
    }

    public void sendDataToServerForProcessing(String params, ImageInterface updateListener){

        if (images == null && src == null){
            return;
        }

        if (images == null) images = new Bitmap[1];
        images[0] = src;

        Log.d("Current images", Arrays.toString(images));

        Endpoints.sendData(images, params, new ImageFetchCallback() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                if (bitmap != null){
                    updateListener.updateImage(bitmap);
                }

            }


            @Override
            public void onFailure(String error) {
                Log.d("ImageError", error);
            }
        });
    }
}

