public abstract class Movie {
    private final String title;

    public Movie(String title) {
        this.title = title;
    }

    public String getTitle (){
        return title;
    }

    public int determineFrequentRenterPoints(int daysRented) {
        return 1;
    }

    public abstract double determineAmount(int daysRented);
}