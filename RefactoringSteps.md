# Refactoring a JavaScript video store
Refactoring Demo based on:

* [https://github.com/unclebob/videostore](https://github.com/unclebob/videostore)
* [https://martinfowler.com/articles/refactoring-video-store-js/](https://martinfowler.com/articles/refactoring-video-store-js/)

# Notes for Trainers

* Explain the Source Code, starting with the tests
* Show classes `Rental`, `Movie`, `Statement`
* Run tests
* Show test results after each refactoring step
* Show coverage
* Explain Shortcuts

# Refactoring steps
1. Rename class `Statement` to `Statement`
2. Rename variable `statement` in `StatementTest` to `statement`
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

10. *Note:* The class `Statement` is now responsible for formatting, `Rental` is responsible for the calculation. Here we applied again the [Single Responsibility Principle](http://www.principles-wiki.net/principles:single_responsibility_principle) on class level, we have the same layer of abstraction ([SLA](http://www.principles-wiki.net/principles:single_level_of_abstraction)) and the method is now short and easy to understand.
   
    The `Statement.rentalLine()` method could now look like this:
    ```javascript
    rentalLine(rental) {
      this.frequentRenterPoints += rental.determineFrequentRenterPoints()
      const amount = rental.determineAmount()
      this.totalAmount += amount
      return this.formatRentalLine(rental, amount)
    }
    ```
11. `Rental.determineAmount()` - The calculation depends on `Movie` and the `MovieType`. It only depends on `daysRented` from `Rental`. Because we have more dependencies to `Movie` than to `Rental`, the implementation of this method might belong to `Movie`. 
    1. Create method `Movie.determineAmount(daysRented)` and delegate to it from `Rental.determineAmount()`.
12. `Rental.determineFrequentRenterPoints()` (Same as above)
    1. Create method `Movie.determineFrequentRenterPoints(daysRented)` and delegate to it. 
13. The methods `Rental.determineAmount()` and `Rental.determineFrequentRenterPoints` could look like this:
    ```javascript
    determineAmount() {
      return this.movie.determineAmount(this.daysRented)
    }

    determineFrequentRenterPoints() {
      return this.movie.determineFrequentRenterPoints(this.daysRented)
    }
    ```

    The methods `Movie.determineAmount(daysRented)` and `Movie.determineFrequentRenterPoints(daysRented)` could look like this:
    ```javascript
    determineAmount(daysRented) {
      let amount = 0
      switch (this.priceCode) {
        case MovieType.REGULAR:
          amount += 2
          if (daysRented > 2) {
            amount += (daysRented - 2) * 1.5
          }
          break
        case MovieType.NEW_RELEASE:
          amount += daysRented * 3
          break
        case MovieType.CHILDRENS:
          amount += 1.5
          if (daysRented > 3) {
            amount += (daysRented - 3) * 1.5
          }
          break
      }
      return amount
    }

    determineFrequentRenterPoints(daysRented) {
      if (this.priceCode === MovieType.NEW_RELEASE && daysRented > 1) {
        return 2
      }
      return 1
    }
    ```
14. `Movie.determineAmount()` - Replace `switch` statement with polymorphism
    1.  Create three classes: `RegularMovie`, `ChildrensMovie` and `NewReleaseMovie` extending `Movie` and replace constructor calls in `videostore.qunit.js` to use the new classes. 
    2. Add and use new constructors for the new classes without `priceCode`
    3. Push down the implementation of `Movie.determineAmount()` to call newly created classes
    4. Run tests and show unused code in coverage
    5. Remove unused cases from `switch` statement for each class
    6. Remove `switch` statement and only keep the only remaining case.
15. `Movie.determineFrequentRenterPoints()`
    1. Keep a general method which is returning `1` for in the `Movie` class
    2. Override method in `NewReleaseMovie` and remove check for `priceCode`
16. `Movie` and its sub classes
    1. Remove `MovieType` and the concept of `priceCode` which is no longer needed.
    2. Remove redundant constructors of sub classes
17. Your final Movie classes might look like this:
    ```javascript
    export class Movie {
      constructor(title) {
        this.title = title
      }

      determineFrequentRenterPoints(daysRented) {
        return 1
      }

      getTitle() {
        return this.title
      }
    }

    export class RegularMovie extends Movie {
      determineAmount(daysRented) {
        let amount = 2
        if (daysRented > 2) {
          amount += (daysRented - 2) * 1.5
        }
        return amount
      }
    }

    export class NewReleaseMovie extends Movie {
      determineAmount(daysRented) {
        return daysRented * 3
      }

      determineFrequentRenterPoints(daysRented) {
        if (daysRented > 1) {
          return 2
        }
        return super.determineFrequentRenterPoints(daysRented)
      }
    }

    export class ChildrensMovie extends Movie {
      determineAmount(daysRented) {
        let amount = 1.5
        if (daysRented > 3) {
          amount += (daysRented - 3) * 1.5
        }
        return amount
      }
    }
    ```

    *Why did we do this?*
      * [Open Closed Principle (OCP)](http://www.principles-wiki.net/principles:open-closed_principle)
        In case of extending the application by adding new movie types, this can now be done by adding new classes (open for extension) instead of adjusting the `Movie` and the `switch` statement (closed for modification). In case of adjusting the `switch` statement, this code would get more complex and more test cases would have to be added for the same code, every time a movie type is added. 

18. Finally the newly created classes can be moved to its own files (e.g. `regular-movie.js`, `childrens-movie.js`, `new-release-movie.js`). You can use the `Move to a new file` refactoring and rename the files depending on your file name conventions. 