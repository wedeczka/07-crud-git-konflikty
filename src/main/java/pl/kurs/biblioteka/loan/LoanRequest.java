package pl.kurs.biblioteka.loan;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LoanRequest(
        @NotNull @Positive Long bookId,
        @NotBlank @Email String readerEmail
) {
}
