package pl.kurs.biblioteka.book;

public record BookResponse(
        Long id,
        String title,
        String isbn,
        int availableCopies,
        AuthorSummary author
) {
    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                book.getAvailableCopies(),
                new AuthorSummary(book.getAuthor().getId(), book.getAuthor().getName())
        );
    }
}