# Refactoring a JavaScript video store
Refactoring Demo based on:

* [https://github.com/unclebob/videostore](https://github.com/unclebob/videostore)
* [https://martinfowler.com/articles/refactoring-video-store-js/](https://martinfowler.com/articles/refactoring-video-store-js/)

# Notes for Trainers

* Explain the Source Code, starting with the tests
* Show classes `Rental`, `Movie`, `Customer`
* Run tests
* Show test results after each refactoring step
* Show coverage
* Explain Shortcuts

# Refactoring steps
1. Rename class `Customer` to `Statement` and `CustomerTest` to `StatementTest`.
2. Rename variable `customer` in `StatementTest` to `statement`
4. Rename method `Statement.statement()` to `Statement.generate()`
5. `Statement.generate()`
   1. Extract method `clearTotals()` (initialization of `totalAmount` and `frequentRenterPoints`)
   2. Rename `result` to `statementText`
   3. Extract method: `header()` (`String statementText = 'Rental Record for ...`)
   4. Rename loop variable `each` to `rental`
   5. Extract method for for-loop: `rentalLines()`
   6. Extract method: `footer()`
    
    *Note:* Make sure this methods do not take the resulting string as a parameter. Create a new string and append it within the `Statement.generate()` method. The resulting method should look like this:
    
    ```java
    public String generate() {
      clearTotals();
      String statementText = header();
      statementText += rentalLines();
      statementText += footer();
      return statementText;
    }
    ```

    *Why did we do this?*
      * The statements of the method have the same or [single layer of abstraction (SLA)](http://www.principles-wiki.net/principles:single_level_of_abstraction). This makes the code easer to read.
      * [Single Responsibility Principle (SRP)](http://www.principles-wiki.net/principles:single_responsibility_principle) on method level. The main responsibility of the method is to concatenate the different parts of the statement.


6. `Statement.rentalLines()`
   1. Extract method `rentalLine()`
   
   The method `rentalLines()` could look like this:
   ```java
   private String rentalLines() {
        return rentals
                .stream()
                .map(this::rentalLine)
                .collect(Collectors.joining(""));
    }
   ```

7. `Statement.rentalLine()`
   1. Extract method `determineAmount(rental)`
   2. Extract method `determineFrequentRenterPoints(rental)`. The method should return the frequent renter points. 
      The assignment to `frequentRenterPoints` should be kept in `Statement.rentalLine()`.
      The goal is to have this function calls symmetric, using assignments which makes it easier to read. 
   3. Extract method `formatRentalLine(rental, rentalAmount)`
   
   After these steps, the method could look like this:
   ```java
   private String rentalLine(Rental rental) {
        double rentalAmount = determineAmount(rental);
        frequentRenterPoints += determineFrequentRenterPoints(rental);
        totalAmount += rentalAmount;
        return formatRentalLine(rental, rentalAmount);
    }
   ```
   
8. `Statement.determineAmount(rental)` - This method is only dependent on class `Rental`
    1.  move to class `Rental` -> `rental.determineAmount()`
9.  `Statement.determineFrequentRenterPoints(rental)` - This method is only dependent on class `Rental`
    1. Move to class `Rental` -> `rental.determineFrequentRenterPoints()`

10. *Note:* The class `Statement` is now responsible for formatting, `Rental` is responsible for the calculation.
    Here we applied again the [Single Responsibility Principle](http://www.principles-wiki.net/principles:single_responsibility_principle) on class level,
    we have the same layer of abstraction ([SLA](http://www.principles-wiki.net/principles:single_level_of_abstraction))
    and the method is now short and easy to understand.
   
    The `Statement.rentalLine()` method could now look like this:
    ```java
    private String rentalLine(Rental rental) {
        frequentRenterPoints += rental.determineFrequentRenterPoints();
        double rentalAmount = rental.determineAmount();
        totalAmount += rentalAmount;
        return formatRentalLine(rental, rentalAmount);
    }
    ```

11. `Rental.determineAmount()` - The calculation depends on `Movie` and the `MovieType`. It only depends on `daysRented` from `Rental`.
    Because we have more dependencies to `Movie` than to `Rental`, the implementation of this method might belong to `Movie`.
    1. Create method `Movie.determineAmount(int daysRented)` and delegate to it from `Rental.determineAmount()`.
12. `Rental.determineFrequentRenterPoints()` (Same as above)
    1. Create method `Movie.determineFrequentRenterPoints(int daysRented)` and delegate to it. 
13. The methods `Rental.determineAmount()` and `Rental.determineFrequentRenterPoints` could look like this:
    ```java
    public double determineAmount() {
        return movie.determineAmount(daysRented);
    }

    public int determineFrequentRenterPoints() {
        return movie.determineFrequentRenterPoints(daysRented);
    }
    ```

    The methods `Movie.determineAmount(int daysRented)` and `Movie.determineFrequentRenterPoints(int daysRented)` could look like this:
    ```java
    public double determineAmount(int daysRented) {
        double thisAmount = 0;
        switch (priceCode) {
            case REGULAR:
                thisAmount += 2;
                if (daysRented > 2)
                    thisAmount += (daysRented - 2) * 1.5;
                break;
            case NEW_RELEASE:
                thisAmount += daysRented * 3;
                break;
            case CHILDRENS:
                thisAmount += 1.5;
                if (daysRented > 3)
                    thisAmount += (daysRented - 3) * 1.5;
                break;
        }
        return thisAmount;
    }

    public int determineFrequentRenterPoints(int daysRented) {
        if (priceCode == NEW_RELEASE && daysRented > 1) {
            return 2;
        }
        return 1;
    }
    ```

14. `Movie.determineAmount()` - Replace `switch` statement with polymorphism
    1.  Create three classes: `RegularMovie`, `ChildrensMovie` and `NewReleaseMovie` extending `Movie` and replace constructor calls in `StatementTest` to use the new classes. 
    2. Add and use new constructors for the new classes without `priceCode`
    3. Push down the implementation of `Movie.determineAmount()` to call newly created classes
    4. Make class `Movie` abstract and add abstract method `Movie.determineAmount()`
    5. Run tests and show unused code in coverage
    6. Remove unused cases from `switch` statement for each class
    7. Remove `switch` statement and only keep the only remaining case.
15. `Movie.determineFrequentRenterPoints()`
    1. Keep a general method which is returning `1` for in the `Movie` class
    2. Override method in `NewReleaseMovie` and remove check for `priceCode`
16. `Movie` and its sub classes
    1. Remove `MovieType` and the concept of `priceCode` which is no longer needed.
17. Your final Movie classes might look like this:
    ```java
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

    public class RegularMovie extends Movie {
        public RegularMovie(String title) {
            super(title);
        }
    
        public double determineAmount(int daysRented) {
            double thisAmount = 2;
            if (daysRented > 2) {
                thisAmount += (daysRented - 2) * 1.5;
            }
            return thisAmount;
        }
    }

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

    public class ChildrensMovie extends Movie {
        public ChildrensMovie(String title) {
            super(title);
        }
    
        public double determineAmount(int daysRented) {
            double thisAmount = 0;
            thisAmount += 1.5;
            if (daysRented > 3) {
                thisAmount += (daysRented - 3) * 1.5;
            }
            return thisAmount;
        }
    }
    ```

    *Why did we do this?*
      * [Open Closed Principle (OCP)](http://www.principles-wiki.net/principles:open-closed_principle):
        In case of extending the application by adding new movie types, this can now be done by adding new classes (open for extension) instead of adjusting `Movie` and the `switch` statement (closed for modification).
        In case of adjusting the `switch` statement, this code would get more complex and more test cases would have to be added for the same code, every time a movie type is added.
        