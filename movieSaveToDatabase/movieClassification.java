import java.sql.*;
import java.util.*;

/**
 * getRandomMovieIdByGenres: randomly recommends a movie by selected genres
 */
public class MovieClassification {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/my_movie_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Movie2025!";

    public static Integer getRandomMovieIdByGenres(List<String> genreNames) {
        if (genreNames == null || genreNames.isEmpty()) {
            System.out.println("Please provide at least one genre.");
            return null;
        }

        List<Integer> movieIds = new ArrayList<>();
        String placeholders = String.join(",", Collections.nCopies(genreNames.size(), "?"));
        String sql = "SELECT DISTINCT movie_id FROM movie_genres WHERE genre_name IN (" + placeholders + ")";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < genreNames.size(); i++) {
                stmt.setString(i + 1, genreNames.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                movieIds.add(rs.getInt("movie_id"));
            }

            if (!movieIds.isEmpty()) {
                Random random = new Random();
                return movieIds.get(random.nextInt(movieIds.size()));
            } else {
                System.out.println("No movies found for the selected genres.");
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static Map<String, Object> getMovieDetails(Integer movieId) {
        if (movieId == null) {
            return null;
        }

        Map<String, Object> movieDetails = new HashMap<>();

        String sql = "SELECT m.*, GROUP_CONCAT(mg.genre_name SEPARATOR ', ') AS genres " +
                     "FROM movies m " +
                     "LEFT JOIN movie_genres mg ON m.movie_id = mg.movie_id " +
                     "WHERE m.movie_id = ? " +
                     "GROUP BY m.movie_id";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movieId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                movieDetails.put("id", rs.getInt("movie_id"));
                movieDetails.put("title", rs.getString("title"));
                movieDetails.put("year", rs.getInt("year"));
                movieDetails.put("director", rs.getString("director"));
                movieDetails.put("description", rs.getString("description"));
                movieDetails.put("genres", rs.getString("genres"));
                return movieDetails;
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving movie details: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public static boolean testDatabaseConnection() {
        System.out.println("Connecting to database...");
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            conn.close();
            System.out.println("Database connected successfully!");
            return true;
        } catch (SQLException e) {
            System.out.println("Warning: Cannot connect to database.");
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        if (!testDatabaseConnection()) {
            System.out.println("Exiting program.");
            return;
        }

        Scanner scanner = new Scanner(System.in);

        System.out.println("===== Movie Recommendation System =====");
        System.out.println("Enter movie genres (comma-separated):");
        System.out.println("Example: Romance,Adventure,Sci-Fi");

        String input = scanner.nextLine();
        List<String> selectedGenres = Arrays.asList(input.split("\\s*,\\s*"));

        System.out.println("Searching for movies in genres: " + String.join(", ", selectedGenres));

        Integer movieId = getRandomMovieIdByGenres(selectedGenres);

        if (movieId != null) {
            System.out.println("Found movie ID: " + movieId);

            Map<String, Object> movieDetails = getMovieDetails(movieId);
            if (movieDetails != null) {
                System.out.println("\n===== Recommended Movie =====");
                System.out.println("Movie ID: " + movieDetails.get("id"));
                System.out.println("Title: " + movieDetails.get("title"));
                System.out.println("Year: " + movieDetails.get("year"));
                System.out.println("Director: " + movieDetails.get("director"));
                System.out.println("Genres: " + movieDetails.get("genres"));
                System.out.println("Description: " + movieDetails.get("description"));
            } else {
                System.out.println("Unable to retrieve movie details.");
            }
        }

        scanner.close();
    }
}
