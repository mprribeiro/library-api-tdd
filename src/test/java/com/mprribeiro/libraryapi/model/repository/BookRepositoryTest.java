package com.mprribeiro.libraryapi.model.repository;

import com.mprribeiro.libraryapi.api.model.entity.Book;
import com.mprribeiro.libraryapi.api.model.repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir o livro na base com o isbn informado")
    public void returnTrueWhenIsbnExists () {
        // cenário
        String isbn = "123";
        Book book = createBook();
        entityManager.persist(book);

        // execução
        boolean isbnExists = repository.existsByIsbn(isbn);

        // verificação
        assertThat(isbnExists).isTrue();
    }

    private Book createBook() {
        return Book.builder().title("Who let the dogs out").author("Lica").isbn("123").build();
    }

    @Test
    @DisplayName("Deve retornar falso quando não existir um livro na base com o isbn informado")
    public void returnFalseWhenIsbnDoesntExist () {
        // cenário
        String isbn = "123";

        // execução
        boolean isbnExists = repository.existsByIsbn(isbn);

        // verificação
        assertThat(isbnExists).isFalse();
    }

    @Test
    @DisplayName("Deve obter um livro por id")
    public void findByIdTest() {

        Book book = createBook();
        entityManager.persist(book);

        Optional<Book> foundBook = repository.findById(book.getId());

        assertThat(foundBook.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest() {

        Book book = createBook();
        entityManager.persist(book);

        Book savedBook = repository.save(book);

        assertThat(savedBook.getId()).isNotNull();
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void deleteBookTest() {

        Book book = createBook();
        entityManager.persist(book);
        Book foundBook =  entityManager.find(Book.class, book.getId());

        repository.delete(foundBook);

        Book deletedBook =  entityManager.find(Book.class, book.getId());

        assertThat(deletedBook).isNull();
    }



}
