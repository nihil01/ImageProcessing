package world.horosho.prictureprocessor.http;


import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpBase {
    public static final String BASE = "https://image.horosho.world/";


    public static RESTService build(){

        return new Retrofit.Builder()
            .baseUrl(BASE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RESTService.class);
    }

    public static RESTService build(String inputURL) {

        return new Retrofit.Builder()
                .baseUrl(inputURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RESTService.class);
    }

}
