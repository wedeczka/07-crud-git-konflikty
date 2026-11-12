package pl.kurs.biblioteka.loan;

public class LoanLimitExceededException extends RuntimeException {

    public LoanLimitExceededException(String readerEmail, int limit) {
        super("Czytelnik " + readerEmail + " przekroczył limit " + limit + " wypożyczeń");
    }
}
