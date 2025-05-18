
public class Movie {
    public int id;
    public String title;

    public Movie(int id, String title) {
        this.id = id;
        this.title = title;
    }

    @Override
    public String toString() {
        return title + " (ID: " + id + ")";
    }
}
