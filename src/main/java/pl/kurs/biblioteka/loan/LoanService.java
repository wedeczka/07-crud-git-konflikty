package pl.kurs.biblioteka.loan;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kurs.biblioteka.book.Book;
import pl.kurs.biblioteka.book.BookService;
import pl.kurs.biblioteka.common.ConflictException;
import pl.kurs.biblioteka.common.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.List;

@Service
public class LoanService {

    private static final int MAX_LOANS_PER_READER = 3;

    private final LoanRepository loanRepository;
    private final BookService bookService;

    public LoanService(LoanRepository loanRepository, BookService bookService) {
        this.loanRepository = loanRepository;
        this.bookService = bookService;
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> findByReader(String readerEmail) {
        return loanRepository.findByReaderEmail(readerEmail).stream()
                .map(LoanResponse::from)
                .toList();
    }

    @Transactional
    public LoanResponse borrow(Long bookId, String readerEmail) {
        Book book = bookService.findById(bookId);
        if (book.getAvailableCopies() == 0) {
            throw new ConflictException("Brak wolnych egzemplarzy książki " + book.getTitle());
        }

        if (loanRepository.countByReaderEmail(readerEmail) >= MAX_LOANS_PER_READER) {
            throw new LoanLimitExceededException(readerEmail, MAX_LOANS_PER_READER);
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        Loan loan = loanRepository.save(new Loan(book, readerEmail, LocalDate.now()));
        return LoanResponse.from(loan);
    }

    @Transactional
    public void giveBack(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono wypożyczenia o id " + loanId));
        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        loanRepository.delete(loan);
    }
}
