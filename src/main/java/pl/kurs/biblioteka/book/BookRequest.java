package pl.kurs.biblioteka.book;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BookRequest(
        @NotBlank String title,
        @NotBlank String isbn,
        @NotNull @Min(0) Integer availableCopies,
        @NotNull @Positive Long authorId
) {
}
