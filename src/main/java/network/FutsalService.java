package network;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface FutsalService {
    @GET("login.php")
    Observable<Response<String>> login(@Query("_") long timestamp, @Query("name") String username, @Query("pass") String password);

    @GET("games.php")
    Observable<Response<String>> getGames(@Header("Cookie") String cookie, @Query("_") long timestamp);

    @GET("status.php")
    Observable<Response<String>> getGameStatus(@Header("Cookie") String cookie, @Query("_") long timestamp, @Query("game") int gameId);

}
