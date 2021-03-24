public class NewReleaseMovie extends Movie {
    public NewReleaseMovie(String title) {
        super(title);
    }

    public int determineFrequentRenterPoints(int daysRented) {
        if (daysRented > 1) {
            return 2;
        }
        return 1;
    }

    public double determineAmount(int daysRented) {
        double thisAmount = 0;
        thisAmount += daysRented * 3;
        return thisAmount;
    }
}
