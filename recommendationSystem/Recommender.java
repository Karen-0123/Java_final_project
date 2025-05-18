
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Recommender {
    private final TMDBClient tmdbClient = new TMDBClient();

    public List<Movie> recommendFromFavorites(List<Integer> favoriteMovieIds) throws IOException {
        List<Movie> recommendations = new ArrayList<>();

        for (int movieId : favoriteMovieIds) {
            List<Movie> similar = tmdbClient.getSimilarMovies(movieId);
            recommendations.addAll(similar);
        }

        // 可以額外去重、排序、篩選等等 or 考慮加權
        return recommendations;
    }
}
