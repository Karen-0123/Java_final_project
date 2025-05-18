
import okhttp3.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TMDBClient {

    private final OkHttpClient client = new OkHttpClient(); // 建立客戶端
    private final ObjectMapper mapper = new ObjectMapper(); // 建立JSON解析器

    public List<Movie> getSimilarMovies(int movieId) throws IOException {
        // url為API請求的網址
        String url = TMDBConfig.BASE_URL + movieId + "/similar?api_key=" + TMDBConfig.API_KEY
                + "&language=en-US&page=1";

        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) { // 失敗就印出錯誤訊息並回傳空列表
                System.out.println("Request failed: " + response);
                return new ArrayList<>();
            }

            JsonNode root = mapper.readTree(response.body().string()); // 將HTTP的回應轉JSON
            List<Movie> similarMovies = new ArrayList<>();

            for (JsonNode movie : root.get("results")) {
                int id = movie.get("id").asInt();
                String title = movie.get("title").asText();
                similarMovies.add(new Movie(id, title));
            }

            return similarMovies;
        }
    }

    // public static void main(String[] args) {
    // String url =
    // "https://api.themoviedb.org/3/search/movie?query=Inception&api_key=" +
    // TMDBConfig.API_KEY;

    // OkHttpClient client = new OkHttpClient();
    // ObjectMapper mapper = new ObjectMapper();

    // Request request = new Request.Builder().url(url).build();

    // try (Response response = client.newCall(request).execute()) {
    // if (response.isSuccessful()) {
    // String json = response.body().string();
    // JsonNode root = mapper.readTree(json);
    // for (JsonNode movie : root.get("results")) {
    // System.out.println(movie.get("title").asText());
    // }
    // } else {
    // System.out.println("HTTP error: " + response.code());
    // }
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }
}
