import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.json.*;




public class MovieApi {
    private static final Map<Integer, String> genreMap = createGenreMap();
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
    private static final String LANGUAGE = "zh-TW"; // Traditional Chinese

    public static void main(String[] args) {
        try {
            String apiKey = "86d39449bb17fd1414156ecdb7e24d8b"; 
            int totalPages = 3; // Adjust number of pages as needed

            JSONArray moviesArray = fetchMovies(apiKey, totalPages);
            
            // Write results to JSON file
            writeToJsonFile(moviesArray, "movies.json");
            
            System.out.println("movies.json created successfully! Total movies: " + moviesArray.length());
            
        } catch (Exception e) {
            System.err.println("Error during execution: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Fetch movies for specified number of pages
     */
    private static JSONArray fetchMovies(String apiKey, int totalPages) throws Exception {
        JSONArray jsonArray = new JSONArray();

        for (int page = 1; page <= totalPages; page++) {
            System.out.println("Fetching page " + page + " of movies...");
            
            String urlStr = BASE_URL + "/movie/popular?api_key=" + apiKey + "&language=" + LANGUAGE + "&page=" + page;
            JSONObject response = makeApiRequest(urlStr);
            JSONArray results = response.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject movie = results.getJSONObject(i);
                int movieId = movie.getInt("id");
                
                // Process movie basic info
                JSONObject movieDetails = processMovieData(movie);
                
                // Get actor data
                JSONArray actorArray = fetchActors(apiKey, movieId);
                movieDetails.put("actors", actorArray);

                jsonArray.put(movieDetails);
                
                // Optional: Add delay to avoid API rate limits
                if (i < results.length() - 1) {
                    Thread.sleep(200);
                }
            }
            
            // Add short delay between pages
            if (page < totalPages) {
                Thread.sleep(1000);
            }
        }
        
        return jsonArray;
    }

    /**
     * Process movie data, extract required fields
     */
    private static JSONObject processMovieData(JSONObject movie) {
        JSONObject movieDetails = new JSONObject();

        // Process title (prioritize Chinese title, use original if not available)
        String title = movie.optString("title", "");
        if (title.isEmpty()) {
            title = movie.optString("original_title", "Unknown Title");
        }
        movieDetails.put("title", title);

        // Process movie overview
        String overview = movie.optString("overview", "");
        if (overview.isEmpty() || overview.equals("null")) {
            overview = "(No Chinese description available)";
        }
        movieDetails.put("overview", overview);

        // Extract release year
        String releaseDate = movie.optString("release_date", "");
        String year = "";
        if (!releaseDate.isEmpty() && !releaseDate.equals("null")) {
            String[] parts = releaseDate.split("-");
            if (parts.length > 0) {
                year = parts[0];
            }
        }
        movieDetails.put("year", year);
        movieDetails.put("release_date", releaseDate);

        // Process movie rating
        double voteAverage = movie.optDouble("vote_average", 0.0);
        movieDetails.put("rating", voteAverage);

        // Process movie genres
        JSONArray genreNames = new JSONArray();
        JSONArray genreIds = movie.optJSONArray("genre_ids");
        if (genreIds != null) {
            for (int j = 0; j < genreIds.length(); j++) {
                int genreId = genreIds.getInt(j);
                String genreName = genreMap.getOrDefault(genreId, "Other");
                genreNames.put(genreName);
            }
        }
        movieDetails.put("genres", genreNames);

        // Add movie link and poster image URL
        movieDetails.put("link", "https://www.themoviedb.org/movie/" + movie.getInt("id"));
        
        String posterPath = movie.optString("poster_path", "");
        if (!posterPath.isEmpty() && !posterPath.equals("null")) {
            movieDetails.put("image", IMAGE_BASE_URL + posterPath);
        } else {
            movieDetails.put("image", "");
        }

        return movieDetails;
    }

    /**
     * Fetch movie actor data
     */
    private static JSONArray fetchActors(String apiKey, int movieId) throws Exception {
        String creditsUrlStr = BASE_URL + "/movie/" + movieId + "/credits?api_key=" + apiKey + "&language=" + LANGUAGE;
        JSONObject creditsObj = makeApiRequest(creditsUrlStr);
        JSONArray castArray = creditsObj.optJSONArray("cast");

        JSONArray actors = new JSONArray();
        if (castArray != null) {
            int maxActors = Math.min(5, castArray.length());
            for (int i = 0; i < maxActors; i++) {
                JSONObject actorObj = castArray.getJSONObject(i);
                String name = actorObj.optString("name", "");
                if (name.isEmpty()) {
                    name = actorObj.optString("original_name", "");
                }
                
                JSONObject actorDetail = new JSONObject();
                actorDetail.put("name", name);
                actorDetail.put("character", actorObj.optString("character", ""));
                
                String profilePath = actorObj.optString("profile_path", "");
                if (profilePath != null && !profilePath.isEmpty() && !profilePath.equals("null")) {
                    actorDetail.put("image", IMAGE_BASE_URL + profilePath);
                } else {
                    actorDetail.put("image", "");
                }
                
                actors.put(actorDetail);
            }
        }

        return actors;
    }

    /**
     * Send API request and return JSON response
     */
    private static JSONObject makeApiRequest(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept-Charset", "UTF-8");
        
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("API request failed, response code: " + responseCode);
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            return new JSONObject(response.toString());
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Write JSON data to file
     */
    private static void writeToJsonFile(JSONArray jsonArray, String fileName) throws IOException {
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(fileName), StandardCharsets.UTF_8)) {
            writer.write(jsonArray.toString(2));
        }
    }

    /**
     * Create mapping between movie genre IDs and names
     * Using genre IDs from TMDB
     */
    private static Map<Integer, String> createGenreMap() {
        Map<Integer, String> genreMap = new HashMap<>();
        // Action, Adventure, Animation, Comedy, Crime
        genreMap.put(28, "Action");
        genreMap.put(12, "Adventure");
        genreMap.put(16, "Animation");
        genreMap.put(35, "Comedy");
        genreMap.put(80, "Crime");
        // Documentary, Drama, Family, Fantasy, History
        genreMap.put(99, "Documentary");
        genreMap.put(18, "Drama");
        genreMap.put(10751, "Family");
        genreMap.put(14, "Fantasy");
        genreMap.put(36, "History");
        // Horror, Music, Mystery, Romance, Science Fiction
        genreMap.put(27, "Horror");
        genreMap.put(10402, "Music");
        genreMap.put(9648, "Mystery");
        genreMap.put(10749, "Romance");
        genreMap.put(878, "Science Fiction");
        // TV Movie, Thriller, War, Western
        genreMap.put(10770, "TV Movie");
        genreMap.put(53, "Thriller");
        genreMap.put(10752, "War");
        genreMap.put(37, "Western");
        return genreMap;
    }
}