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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public ArrayList<String> getGenres() {
        return genres;
    }

    public void setGenres(ArrayList<String> genres) {
        this.genres = genres;
    }

    public Boolean getStar() {
        return star;
    }

    public void setStar(Boolean star) {
        this.star = star;
    }
}
