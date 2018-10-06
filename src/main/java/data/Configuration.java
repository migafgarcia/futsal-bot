package data;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Configuration {

    @SerializedName("token")
    @Expose
    public String token;
    @SerializedName("username")
    @Expose
    public String username;
    @SerializedName("pw")
    @Expose
    public String pw;
    @SerializedName("base_url")
    @Expose
    public String baseUrl;

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public String getPw() {
        return pw;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}