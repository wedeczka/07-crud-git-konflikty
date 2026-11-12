# Biblioteka: poprawki w CRUD-zie i praca z Pull Requestem

Dostajesz małą aplikację Spring Boot do obsługi biblioteki: autorzy, książki i wypożyczenia. Aplikacja się uruchamia i "jakoś działa", ale zespół frontendowy i użytkownicy zgłosili kilka problemów. Twoim zadaniem jest je naprawić **i dostarczyć poprawki tak, jak w prawdziwym zespole**: na własnym branchu, w Pull Requeście, który przejdzie review.

## Wymagania

- Java 21
- Maven 3.9+ (albo wrapper z IDE)
- git, konto na GitHubie

Stos: Spring Boot 4.1, Spring Data JPA (Hibernate 7), baza H2 w pamięci (dane startowe w `src/main/resources/data.sql`).

## Uruchomienie

```bash
mvn spring-boot:run
```

Aplikacja wstaje na `http://localhost:8080`. Konsola bazy: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:library`, użytkownik `sa`, bez hasła).

Przykładowe wywołania:

```bash
curl -i http://localhost:8080/authors/1
curl -i http://localhost:8080/books
curl -i -X POST http://localhost:8080/books \
     -H 'Content-Type: application/json' \
     -d '{"title":"Pokój","isbn":"978-83-00-00000-1","availableCopies":2,"authorId":1}'
curl -i -X POST http://localhost:8080/loans \
     -H 'Content-Type: application/json' \
     -d '{"bookId":3,"readerEmail":"jan@example.com"}'
```

## Zgłoszenia

**1. "Odpowiedź z API jest gigantyczna i nie da się jej sparsować"** (frontend)
> `GET /authors/1` i `GET /books` zwracają status 200, ale JSON jest ogromny, powtarza w kółko te same dane i na końcu jest uszkodzony. Przeglądarka nie potrafi go odczytać. To samo dzieje się przy wypożyczeniach.

**2. "API kłamie statusami"** (frontend)
> Nieistniejąca książka zwraca 400, jakbyśmy to my wysłali zły request. Utworzenie zasobu zwraca 200 i nie wiemy, pod jakim adresem go szukać. Książkę z pustym tytułem da się zapisać. Duplikat ISBN kończy się 400 z komunikatem, w którym jest kawałek SQL-a. Usunięcie książki, która nie istnieje, zwraca 200.

**3. "Dostałem błąd, a książka i tak jest na moim koncie"** (czytelnik, dział obsługi)
> Czytelnik wypożyczył 3 książki, przy czwartej dostał komunikat o przekroczonym limicie. Mimo to w systemie widnieje czwarte wypożyczenie, a liczba wolnych egzemplarzy tej książki spadła.

## Oczekiwane zachowanie API

| Operacja | Sukces | Błędy |
|---|---|---|
| `GET /authors`, `GET /books` | 200 | |
| `GET /authors/{id}`, `GET /books/{id}` | 200 | 404 gdy nie istnieje |
| `POST /authors`, `POST /books` | 201 + nagłówek `Location` | 400 przy niepoprawnych danych, 404 gdy autor nie istnieje, 409 przy duplikacie ISBN |
| `DELETE /books/{id}` | 204 | 404 gdy nie istnieje |
| `GET /loans?readerEmail=...` | 200 | |
| `POST /loans` | 201 + `Location` | 400 przy niepoprawnych danych, 404 gdy książka nie istnieje, 409 gdy brak egzemplarzy lub przekroczony limit |
| `DELETE /loans/{id}` (zwrot) | 204 | 404 gdy nie istnieje |

Dodatkowo:
- odpowiedź z błędem ma czytelny komunikat dla człowieka i **nie zawiera** szczegółów bazy danych ani stack trace'ów,
- odpowiedź autora zawiera listę jego książek (przynajmniej id i tytuł), odpowiedź książki zawiera id i imię autora,
- nieudana operacja nie może zostawić bazy w połowie zmienionej.

## Wymagania do rozwiązania

1. Napraw wszystkie trzy zgłoszenia. Zanim zaczniesz poprawiać, odtwórz każdy problem (`curl`, test, postman) i zrozum jego przyczynę. W opisie PR-a napisz w 1-2 zdaniach, co było przyczyną każdego z nich.
2. Dopisz testy, które pilnują naprawionego zachowania. Minimum:
   - testy statusów HTTP dla ścieżek z tabeli powyżej (np. `MockMvc`),
   - test, który udowadnia, że wypożyczenie ponad limit **nie zmienia** stanu bazy.
3. `mvn test` przechodzi.

## Jak oddać zadanie


1. Utwórz własny branch od `master`, np. `jan-kowalski/poprawki-api`. Nie commituj bezpośrednio na `master`.
2. Pracuj małymi commitami z sensownymi opisami (jedna poprawka = jeden commit to dobra zasada).
3. Wypchnij branch do swojego forka i otwórz **Pull Request** do `master` w tym repozytorium. W opisie PR-a: co było nie tak, co zmieniłeś, jak to sprawdziłeś.
4. Odpowiadaj na komentarze z review, poprawki dopychaj kolejnymi commitami na ten sam branch.

**Uwaga:** w trakcie review na `master` mogą trafić zmiany innych osób z zespołu, tak jak w każdym prawdziwym projekcie. PR zostanie zmergowany tylko wtedy, gdy nie ma konfliktów i testy przechodzą. Jeśli GitHub pokaże konflikty, pobierz najnowszy `master` z `upstream`, scal go ze swoim branchem (merge albo rebase), rozwiąż konflikty tak, żeby **nie zgubić ani swoich poprawek, ani zmian kolegów**, uruchom testy i wypchnij wynik.
