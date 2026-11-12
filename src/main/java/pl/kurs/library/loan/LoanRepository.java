package pl.kurs.library.loan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByReaderEmail(String readerEmail);

    long countByReaderEmail(String readerEmail);
}
