package world.horosho.prictureprocessor.http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Endpoints {

    private static final RESTService service = HttpBase.build();

    private static UUID generateImageName(byte[] b){
        return UUID.nameUUIDFromBytes(b);
    }

    public static void sendData(Bitmap[] images, String parameter, ImageFetchCallback cb){
        List<MultipartBody.Part> imageParts = new ArrayList<>();

        for (Bitmap image: images){

            Log.d("imageRes","Image here! SIze is " + image.getByteCount());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            Log.d("imageRes", "Now is: " + baos.toByteArray().length);

            byte[] rawImage = baos.toByteArray();
            RequestBody req = RequestBody.create(MediaType.parse("image/jpeg"), rawImage);
            imageParts.add(MultipartBody.Part.createFormData("files",
                    generateImageName(rawImage).toString()+".jpeg", req));
        }

        service.uploadImage(imageParts, parameter).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        // Decode bytes into Bitmap
                        Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                        Log.d("ResponseStatus", "SUCCESSS !!213!");

                        if (bitmap != null) {
                            cb.onSuccess(bitmap);
                            Log.d("ResponseStatus", "SUCCESSS !!!");

                        } else {
                            cb.onFailure("Failed to decode image bytes");

                        }

                    } else {

                        Log.e("ResponseStatus", "NOT SUCCESSFUL! Code: " + response.code() + response.message() );
                        cb.onFailure("Response not successful: " + response.code());

                    }
                } catch (Exception e) {
                    Log.e("ResponseStatus", "Error: " + e.getMessage());
                    cb.onFailure("Error processing response: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                try {
                    if (throwable != null){
                        Log.e("ResponseStatus", throwable.getMessage());
                    }
                } catch (Exception e) {
                    Log.e("ResponseStatus", e.getMessage());
                }
            }
        });
    }

    public static void fetchImageByURL(String url, ImageFetchCallback cb){
        try {
            URI uri = new URI(url);
            String baseUrl = uri.getScheme() + "://" + uri.getAuthority() + "/";
            HttpBase.build(baseUrl).getImageData(url).enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()){
                        if (response.body() != null){

                            Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());

                            cb.onSuccess(bmp);
                        }else{
                            cb.onFailure("Response body is null!");
                        }
                    }else{
                        cb.onFailure("Response just not successful!");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                    cb.onFailure("Response Failure!");
                }
            });


        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

}
