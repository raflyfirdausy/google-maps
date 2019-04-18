package pmo2.kelompok4.googlemapskelompok4.Network;

        import pmo2.kelompok4.googlemapskelompok4.Response.ResponseRoute;
        import retrofit2.Call;
        import retrofit2.http.GET;
        import retrofit2.http.Query;

public interface ApiServices {
    @GET("json")
    Call<ResponseRoute> request_route(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("key") String api_key,
            @Query("language") String language
    );
}
