import java.util.ArrayList;
import java.util.List;

class Statement {
    private String _name;
    private List<Rental> _rentals = new ArrayList<>();
    private double totalAmount;
    private int frequentRenterPoints;

    public Statement(String name) {
        _name = name;
    }

    public void addRental(Rental arg) {
        _rentals.add(arg);
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
        String rentalLines = "";
        for (Rental rental : _rentals) {
            rentalLines += rentalLine(rental);
        }
        return rentalLines;
    }

    private String rentalLine(Rental rental) {

        double thisAmount = 0;
        //determine amounts for each line
        switch (rental.getMovie().getPriceCode()) {
            case Movie.REGULAR:
                thisAmount += 2;
                if (rental.getDaysRented() > 2)
                    thisAmount += (rental.getDaysRented() - 2) * 1.5;
                break;
            case Movie.NEW_RELEASE:
                thisAmount += rental.getDaysRented() * 3;
                break;
            case Movie.CHILDRENS:
                thisAmount += 1.5;
                if (rental.getDaysRented() > 3)
                    thisAmount += (rental.getDaysRented() - 3) * 1.5;
                break;
        }
        // add frequent renter points
        frequentRenterPoints++;
        // add bonus for a two day new release rental
        if ((rental.getMovie().getPriceCode() == Movie.NEW_RELEASE)
                &&
                rental.getDaysRented() > 1) frequentRenterPoints++;
        //show figures for this rental
        totalAmount += thisAmount;
        return "\t" + rental.getMovie().getTitle() + "\t" +
                thisAmount + "\n";
    }

    private void clearTotals() {
        totalAmount = 0;
        frequentRenterPoints = 0;
    }
}