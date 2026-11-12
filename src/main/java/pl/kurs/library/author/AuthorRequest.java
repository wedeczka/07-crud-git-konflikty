package pl.kurs.library.author;

import jakarta.validation.constraints.NotBlank;

public record AuthorRequest(@NotBlank String name) {
}
