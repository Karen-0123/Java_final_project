package compile;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        List<Integer> favoriteIds = List.of(550, 278); // 例如 Fight Club, Shawshank Redemption 的 TMDB ID

        Recommender recommender = new Recommender();
        List<Movie> recommended = recommender.recommendFromFavorites(favoriteIds);

        System.out.println("推薦電影：");
        for (Movie title : recommended) {
            System.out.println("- " + title);
        }
    }
}
