package pl.kurs.biblioteka;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.kurs.biblioteka.author.AuthorRepository;
import pl.kurs.biblioteka.book.Book;
import pl.kurs.biblioteka.book.BookRepository;
import pl.kurs.biblioteka.loan.LoanRepository;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LoanTransactionTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Test
    void fourthLoanMustNotChangeDatabase() throws Exception {
        String uniqueId = UUID.randomUUID().toString();
        String readerEmail = "limit-" + uniqueId + "@example.com";

        Book book = bookRepository.save(new Book(
                "Książka do testu limitu",
                "isbn-test-limit-" + uniqueId,
                5,
                authorRepository.findById(1L).orElseThrow()
        ));

        String requestBody = "{\"bookId\":" + book.getId()
                + ",\"readerEmail\":\"" + readerEmail + "\"}";

        for (int i = 0; i < 3; i++) {
            mvc.perform(post("/loans")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated());
        }

        assertEquals(3, loanRepository.countByReaderEmail(readerEmail));
        assertEquals(2, bookRepository.findById(book.getId()).orElseThrow().getAvailableCopies());

        mvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.blad", containsString("limit")));

        // Ważne: test NIE jest @Transactional. Odczytujemy bazę po zakończeniu requestu.
        assertEquals(3, loanRepository.countByReaderEmail(readerEmail),
                "Czwarte wypożyczenie nie może zostać zapisane");
        assertEquals(2, bookRepository.findById(book.getId()).orElseThrow().getAvailableCopies(),
                "Nieudane wypożyczenie nie może zmniejszyć liczby wolnych egzemplarzy");
    }
}
