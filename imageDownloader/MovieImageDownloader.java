import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import org.json.*;

public class MovieImageDownloader {
    private static final String API_KEY = "86d39449bb17fd1414156ecdb7e24d8b";
    private static final String BASE_URL = "https://image.tmdb.org/t/p/w500";
    private static final String MOVIE_API = "https://api.themoviedb.org/3/movie/";
    private static final String OUTPUT_DIR = "movie_images";

    public MovieImageDownloader() {
        createOutputDirectory();
    }

    private void createOutputDirectory() {
        new File(OUTPUT_DIR + "/posters").mkdirs();
        new File(OUTPUT_DIR + "/actors").mkdirs();
    }

    public void downloadByMovieId(int movieId) {
        try {
            
            JSONObject movie = fetchJsonFromUrl(MOVIE_API + movieId + "?api_key=" + API_KEY);
            JSONObject credits = fetchJsonFromUrl(MOVIE_API + movieId + "/credits?api_key=" + API_KEY);

            if (movie == null || credits == null) {
                System.out.println("Failed to retrieve movie data.");
                return;
            }

            String title = movie.optString("title", "unknown");
            int year = movie.optString("release_date", "").length() >= 4
                    ? Integer.parseInt(movie.getString("release_date").substring(0, 4))
                    : 0;
            String posterPath = movie.optString("poster_path", "");

            if (!posterPath.isEmpty()) {
                String posterUrl = BASE_URL + posterPath;
                String filename = OUTPUT_DIR + "/posters/" + sanitize(title) + "_" + year + ".jpg";
                if (downloadImage(posterUrl, filename)) {
                    System.out.println("Poster saved: " + filename);
                }
            } else {
                System.out.println("No poster found.");
            }

            JSONArray castArray = credits.optJSONArray("cast");
            if (castArray != null) {
                for (int i = 0; i < Math.min(5, castArray.length()); i++) {
                    JSONObject actor = castArray.getJSONObject(i);
                    String name = actor.optString("name", "unknown");
                    String character = actor.optString("character", "unknown");
                    String profilePath = actor.optString("profile_path", "");

                    if (!profilePath.isEmpty()) {
                        String actorUrl = BASE_URL + profilePath;
                        String filename = OUTPUT_DIR + "/actors/" + sanitize(name) + ".jpg";
                        if (downloadImage(actorUrl, filename)) {
                            System.out.println("Downloaded actor: " + name + " as " + character);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Error downloading movie by ID: " + e.getMessage());
        }
    }

    private JSONObject fetchJsonFromUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (InputStream in = conn.getInputStream()) {
                String text = new String(in.readAllBytes());
                return new JSONObject(text);
            }
        }
        return null;
    }

    private boolean downloadImage(String urlString, String destinationPath) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (
                    InputStream in = conn.getInputStream();
                    FileOutputStream out = new FileOutputStream(destinationPath)
                ) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    return true;
                }
            } else {
                System.out.println("HTTP error: " + conn.getResponseCode());
            }
        } catch (Exception e) {
            System.out.println("Error downloading image: " + e.getMessage());
        }
        return false;
    }

    private String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
    }
}
