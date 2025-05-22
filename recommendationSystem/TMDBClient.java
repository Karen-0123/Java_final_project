package compile;

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
                + "&language=zh-TW&page=1";

        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) { // 失敗就印出錯誤訊息並回傳空列表
                System.out.println("Request failed: " + response);
                return new ArrayList<>();
            }

            JsonNode root = mapper.readTree(response.body().string()); // 將HTTP的回應轉JSON
            List<Movie> similarMovies = new ArrayList<>();

            for (JsonNode movie : root.get("results")) {
                String title = movie.get("title").asText();
                String posterPath = "https://image.tmdb.org/t/p/w440_andh660_face" + movie.get("poster_path").asText();
                String link = "https://www.themoviedb.org/movie/" + movie.get("id").asInt();
                String ID = movie.get("id").asText();
                String overview = movie.get("overview").asText();
                ArrayList<String> genres = new ArrayList<>();
                JsonNode genreArray = movie.get("genre_ids");
                for (JsonNode genreIdNode : genreArray) { // 例如 28 是一個 JsonNode
                    String genreId = genreIdNode.asText();
                    genres.add(genreId);
                }
                similarMovies.add(new Movie(title, posterPath, link, ID, overview, genres, false));
            }
            return similarMovies;
        }
    }
}
