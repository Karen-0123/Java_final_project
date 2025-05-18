import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class MovieImageDownloader {
    private static final String BASE_URL = "https://image.tmdb.org/t/p/w500";
    private static final String OUTPUT_DIR = "movie_images";
    private List<Movie> movies = new ArrayList<>();

    public static void main(String[] args) {
        MovieImageDownloader downloader = new MovieImageDownloader();
        downloader.loadMoviesFromFile("movieDataCrawler/movies.json");
        downloader.showMenu();
    }

    public MovieImageDownloader() {
        createOutputDirectory();
    }

    private void createOutputDirectory() {
        File posterDir = new File("movie_images/posters");
        File actorDir = new File("movie_images/actors");

        if (posterDir.mkdirs()) {
            System.out.println("Poster directory created.");
        }
        if (actorDir.mkdirs()) {
            System.out.println("Actor directory created.");
        }
    }

    public void loadMoviesFromFile(String pathStr) {
        try {
            Path path = Paths.get(pathStr);
            if (!Files.exists(path)) {
                path = Paths.get("..", pathStr);
                if (!Files.exists(path)) {
                    System.err.println("Could not find the file: " + pathStr);
                    return;
                }
            }

            String content = Files.readString(path, StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(content);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String title = obj.optString("title", "Unknown");
                String image = obj.optString("image", "");
                int year = obj.optInt("year", 0);
                JSONArray actorArray = obj.optJSONArray("actors");
                List<Actor> actors = new ArrayList<>();

                if (actorArray != null) {
                    for (int j = 0; j < actorArray.length(); j++) {
                        JSONObject actorObj = actorArray.getJSONObject(j);
                        String name = actorObj.optString("name", "Unknown");
                        String character = actorObj.optString("character", "Unknown");
                        String actorImg = actorObj.optString("image", "");
                        if (!actorImg.isEmpty()) {
                            actors.add(new Actor(name, character, actorImg));
                        }
                    }
                }

                movies.add(new Movie(title, year, image, actors));
            }

            System.out.println("Loaded " + movies.size() + " movies.");
        } catch (IOException e) {
            System.err.println("IO error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showMenu() {
        Scanner scanner = new Scanner(System.in);
        int choice = 0;

        while (choice != 3) {
            System.out.println("\n===== Movie Image Downloader =====");
            System.out.println("1. Download movie poster");
            System.out.println("2. Download actor photos");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");

            try {
                choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1 -> downloadMoviePoster(scanner);
                    case 2 -> downloadMovieActors(scanner);
                    case 3 -> System.out.println("Exiting...");
                    default -> System.out.println("Invalid choice.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number.");
            }
        }

        scanner.close();
    }

    private void downloadMoviePoster(Scanner scanner) {
        displayMovieList();
        System.out.print("Enter movie number: ");
        try {
            int index = Integer.parseInt(scanner.nextLine()) - 1;
            if (index < 0 || index >= movies.size()) {
                System.out.println("Invalid movie number.");
                return;
            }

            Movie movie = movies.get(index);
            String imageUrl = movie.imageUrl;
            if (imageUrl == null || imageUrl.isEmpty()) {
                System.out.println("No poster URL for this movie.");
                return;
            }

            if (!imageUrl.startsWith("http")) {
                imageUrl = BASE_URL + imageUrl;
            }

            String filename = "movie_images/posters/" + sanitize(movie.title) + "_" + movie.year + ".jpg";
            if (downloadImage(imageUrl, filename)) {
                System.out.println("Poster saved at: " + filename);
            } else {
                System.out.println("Failed to download poster.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }
    }

    private void downloadMovieActors(Scanner scanner) {
        displayMovieList();
        System.out.print("Enter movie number: ");
        try {
            int index = Integer.parseInt(scanner.nextLine()) - 1;
            if (index < 0 || index >= movies.size()) {
                System.out.println("Invalid movie number.");
                return;
            }

            Movie movie = movies.get(index);
            List<Actor> actors = movie.actors;

            if (actors.isEmpty()) {
                System.out.println("No actors with photos.");
                return;
            }

            for (int i = 0; i < actors.size(); i++) {
                System.out.println((i + 1) + ". " + actors.get(i).name + " as " + actors.get(i).character);
            }

            System.out.print("Enter actor number (0 = all): ");
            int actorChoice = Integer.parseInt(scanner.nextLine());

            if (actorChoice == 0) {
                for (Actor actor : actors) {
                    String url = actor.imageUrl;
                    if (!url.startsWith("http")) {
                        url = BASE_URL + url;
                    }
                    String filename = "movie_images/actors/" + sanitize(actor.name) + ".jpg";
                    if (downloadImage(url, filename)) {
                        System.out.println("Downloaded: " + actor.name);
                    }
                }
            } else if (actorChoice > 0 && actorChoice <= actors.size()) {
                Actor actor = actors.get(actorChoice - 1);
                String url = actor.imageUrl;
                if (!url.startsWith("http")) {
                    url = BASE_URL + url;
                }
                String filename = "movie_images/actors/" + sanitize(actor.name) + ".jpg";
                if (downloadImage(url, filename)) {
                    System.out.println("Downloaded: " + actor.name);
                } else {
                    System.out.println("Download failed.");
                }
            } else {
                System.out.println("Invalid actor number.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }
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
            System.out.println("Error downloading: " + e.getMessage());
        }
        return false;
    }

    private String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
    }

    private void displayMovieList() {
        for (int i = 0; i < movies.size(); i++) {
            System.out.println((i + 1) + ". " + movies.get(i).title + " (" + movies.get(i).year + ")");
        }
    }

    // Inner classes
    private static class Movie {
        String title;
        int year;
        String imageUrl;
        List<Actor> actors;

        Movie(String title, int year, String imageUrl, List<Actor> actors) {
            this.title = title;
            this.year = year;
            this.imageUrl = imageUrl;
            this.actors = actors;
        }
    }

    private static class Actor {
        String name;
        String character;
        String imageUrl;

        Actor(String name, String character, String imageUrl) {
            this.name = name;
            this.character = character;
            this.imageUrl = imageUrl;
        }
    }
}
