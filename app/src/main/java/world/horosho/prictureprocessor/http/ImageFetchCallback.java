package world.horosho.prictureprocessor.http;

import android.graphics.Bitmap;

import java.io.InputStream;

public interface ImageFetchCallback {
    void onSuccess(Bitmap bitmap);
    void onFailure(String error);
}
