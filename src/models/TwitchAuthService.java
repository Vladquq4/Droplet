package models;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import controllers.StoreController;

public class TwitchAuthService {
    private static final String CLIENT_ID = "qfh338mick9g9udt74l7m4fdehsytw";
    private static final String CLIENT_SECRET = "n7vergdvxn49v1rym3fau056wjd381";
    private static final String TOKEN_URL = "https://id.twitch.tv/oauth2/token";
    private static final String API_URL = "https://api.igdb.com/v4/games";
    private static final String ACCESS_TOKEN = getAccessToken();

    public static String getAccessToken() {
        try {
            String requestBody = "client_id=" + CLIENT_ID +
                    "&client_secret=" + CLIENT_SECRET +
                    "&grant_type=client_credentials";

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TOKEN_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            return jsonResponse.get("access_token").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String getGameCover(String gameTitle) {
        try {
            String requestBody = "fields name, cover.url; search \"" + gameTitle + "\";";
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Client-ID", CLIENT_ID)
                    .header("Authorization", "Bearer " + ACCESS_TOKEN)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonArray jsonResponse = JsonParser.parseString(response.body()).getAsJsonArray();

            if (jsonResponse.size() > 0) {
                JsonObject firstGame = jsonResponse.get(0).getAsJsonObject();
                if (firstGame.has("cover")) {
                    JsonObject cover = firstGame.getAsJsonObject("cover");
                    String coverUrl = "https:" + cover.get("url").getAsString().replace("t_thumb", "t_cover_big");
                    return coverUrl;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
