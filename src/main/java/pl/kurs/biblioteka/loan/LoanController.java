package pl.kurs.biblioteka.loan;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping
    public List<LoanResponse> getByReader(@RequestParam String readerEmail) {
        return loanService.findByReader(readerEmail);
    }

    @PostMapping
    public LoanResponse borrow(@RequestBody LoanRequest request) throws LoanLimitExceededException {
        return loanService.borrow(request.bookId(), request.readerEmail());
    }

    @DeleteMapping("/{id}")
    public void giveBack(@PathVariable Long id) {
        loanService.giveBack(id);
    }
}