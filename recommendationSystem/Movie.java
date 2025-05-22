package compile;

import java.util.ArrayList;
import java.util.List;

public class Movie {
    private String title;
    private String posterPath;
    private String ID;
    private String link;
    private String overview;
    private ArrayList<String> genres;
    private Boolean star;

    public Movie(String title, String posterPath, String link, String ID, String overview, ArrayList<String> genres,
            Boolean star) {
        this.title = title;
        this.posterPath = posterPath;
        this.ID = ID;
        this.link = link;
        this.overview = overview;
        this.genres = genres;
        this.star = star;
    }

    @Override
    public String toString() {
        return title + " (ID: " + ID + ")";
    }
}
