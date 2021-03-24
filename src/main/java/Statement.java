import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class Statement {
    private String _name;
    private List<Rental> rentals = new ArrayList<>();
    private double totalAmount;
    private int frequentRenterPoints;

    public Statement(String name) {
        _name = name;
    }

    public void addRental(Rental arg) {
        rentals.add(arg);
    }

    public String getName() {
        return _name;
    }

    public String generate() {
        clearTotals();
        String statementText = "Rental Record for " + getName() + "\n";
        statementText += rentalLines();
        statementText += footer();
        return statementText;
    }

    private String footer() {
        String footer = "Amount owed is " + totalAmount +
                "\n";
        footer += "You earned " + frequentRenterPoints + " frequent renter points";
        return footer;
    }

    private String rentalLines() {
        return rentals
                .stream()
                .map(this::rentalLine)
                .collect(Collectors.joining(""));
    }

    private String rentalLine(Rental rental) {
        frequentRenterPoints += rental.determineFrequentRenterPoints();
        double rentalAmount = rental.determineAmount();
        totalAmount += rentalAmount;
        return formatRentalLine(rental, rentalAmount);
    }

    private String formatRentalLine(Rental rental, double thisAmount) {
        return "\t" + rental.getMovie().getTitle() + "\t" +
                thisAmount + "\n";
    }

    private void clearTotals() {
        totalAmount = 0;
        frequentRenterPoints = 0;
    }
}