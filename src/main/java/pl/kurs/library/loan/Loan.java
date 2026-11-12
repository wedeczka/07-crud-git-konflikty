package pl.kurs.library.loan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import pl.kurs.library.book.Book;

import java.time.LocalDate;

@Entity
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Book book;

    @Column(nullable = false)
    private String readerEmail;

    @Column(nullable = false)
    private LocalDate loanDate;

    protected Loan() {
    }

    public Loan(Book book, String readerEmail, LocalDate loanDate) {
        this.book = book;
        this.readerEmail = readerEmail;
        this.loanDate = loanDate;
    }

    public Long getId() {
        return id;
    }

    public Book getBook() {
        return book;
    }

    public String getReaderEmail() {
        return readerEmail;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }
}
