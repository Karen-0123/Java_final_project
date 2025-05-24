public class TMDBDownloadertest {
    public static void main(String[] args) {
        MovieImageDownloader downloader = new MovieImageDownloader();

       
        int testMovieId = 550;
        downloader.downloadByMovieId(testMovieId);
    }
}
