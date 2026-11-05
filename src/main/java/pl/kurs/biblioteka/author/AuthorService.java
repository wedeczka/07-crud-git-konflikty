package pl.kurs.biblioteka.author;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    // mapowanie na DTO wewnątrz transakcji -> lista książek (LAZY) zdąży się doczytać
    @Transactional(readOnly = true)
    public List<AuthorResponse> findAll() {
        return authorRepository.findAll().stream()
                .map(AuthorResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AuthorResponse getById(Long id) {
        return AuthorResponse.from(findById(id));
    }

    // encja - używana wewnętrznie przez BookService
    public Author findById(Long id) {
        return authorRepository.findById(id).get();
    }

    @Transactional
    public AuthorResponse create(AuthorRequest request) {
        return AuthorResponse.from(authorRepository.save(new Author(request.name())));
    }
}