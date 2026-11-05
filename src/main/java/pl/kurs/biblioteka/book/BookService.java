package pl.kurs.biblioteka.book;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kurs.biblioteka.author.Author;
import pl.kurs.biblioteka.author.AuthorService;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorService authorService;

    public BookService(BookRepository bookRepository, AuthorService authorService) {
        this.bookRepository = bookRepository;
        this.authorService = authorService;
    }

    @Transactional(readOnly = true)
    public List<BookResponse> findAll() {
        return bookRepository.findAll().stream()
                .map(BookResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookResponse getById(Long id) {
        return BookResponse.from(findById(id));
    }

    public Book findById(Long id) {
        return bookRepository.findById(id).get();
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        Author author = authorService.findById(request.authorId());
        Book book = new Book(request.title(), request.isbn(), request.availableCopies(), author);
        return BookResponse.from(bookRepository.save(book));
    }

    @Transactional
    public void delete(Long id) {
        bookRepository.deleteById(id);
    }
}