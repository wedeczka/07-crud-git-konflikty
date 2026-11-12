package pl.kurs.biblioteka;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiHttpStatusTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void shouldReturn200ForListsAndExistingResources() throws Exception {
        mvc.perform(get("/authors")).andExpect(status().isOk());
        mvc.perform(get("/books")).andExpect(status().isOk());
        mvc.perform(get("/authors/1")).andExpect(status().isOk());
        mvc.perform(get("/books/1")).andExpect(status().isOk());
        mvc.perform(get("/loans").param("readerEmail", "http-test@example.com"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenResourceDoesNotExist() throws Exception {
        mvc.perform(get("/authors/999999")).andExpect(status().isNotFound());
        mvc.perform(get("/books/999999")).andExpect(status().isNotFound());
        mvc.perform(delete("/books/999999")).andExpect(status().isNotFound());
        mvc.perform(delete("/loans/999999")).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateAuthorWith201AndLocation() throws Exception {
        mvc.perform(post("/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Nowy autor HTTP"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/authors/")))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void shouldRejectBlankAuthorName() throws Exception {
        mvc.perform(post("/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"  "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.blad").exists());
    }

    @Test
    void shouldCreateBookWith201AndLocation() throws Exception {
        mvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Nowa książka HTTP","isbn":"978-00-http-1","availableCopies":2,"authorId":1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/books/")))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void shouldRejectInvalidBookAndMissingAuthor() throws Exception {
        mvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":" ","isbn":"unique-invalid","availableCopies":2,"authorId":1}
                                """))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Ważny tytuł","isbn":"unique-missing-author","availableCopies":2,"authorId":999999}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn409ForDuplicateIsbnWithoutSqlInResponse() throws Exception {
        mvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Duplikat","isbn":"978-83-08-04907-1","availableCopies":1,"authorId":1}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.blad", containsString("ISBN")));
    }

    @Test
    void shouldDeleteBookWith204() throws Exception {
        String location = mvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Do usunięcia","isbn":"978-00-http-delete","availableCopies":1,"authorId":1}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        assertNotNull(location);
        mvc.perform(delete(location)).andExpect(status().isNoContent());
    }

    @Test
    void shouldBorrowAndReturnWithCorrectStatuses() throws Exception {
        String location = mvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bookId":3,"readerEmail":"http-test@example.com"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/loans/")))
                .andReturn().getResponse().getHeader("Location");
        assertNotNull(location);
        mvc.perform(delete(location)).andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectInvalidLoanAndMissingBook() throws Exception {
        mvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bookId":1,"readerEmail":"bad-email"}
                                """))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bookId":999999,"readerEmail":"http-test@example.com"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn409WhenNoCopiesAreAvailable() throws Exception {
        String location = mvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Zerowy nakład","isbn":"978-00-http-no-copies","availableCopies":0,"authorId":1}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        assertNotNull(location);
        long id = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        mvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":" + id + ",\"readerEmail\":\"http-test@example.com\"}"))
                .andExpect(status().isConflict());
    }
}
