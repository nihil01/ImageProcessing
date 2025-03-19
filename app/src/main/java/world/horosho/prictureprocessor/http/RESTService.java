package world.horosho.prictureprocessor.http;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

public interface RESTService {
    @Multipart
    @POST("files")
    Call<ResponseBody> uploadImage(
            @Part List<MultipartBody.Part> files,
            @Part("parameter") String parameter
    );

    @GET
    Call<ResponseBody> getImageData(@Url String url);
}
