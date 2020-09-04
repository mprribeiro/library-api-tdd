package com.mprribeiro.libraryapi.service;

import com.mprribeiro.libraryapi.api.exception.BusinessException;
import com.mprribeiro.libraryapi.api.model.entity.Book;
import com.mprribeiro.libraryapi.api.model.repository.BookRepository;
import com.mprribeiro.libraryapi.api.service.BookService;
import com.mprribeiro.libraryapi.api.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService service;

    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp() {
        this.service = new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar o livro")
    public void saveBookTest() {
        // cenário
        Book book = createNewBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);
        Mockito.when(repository.save(book)).thenReturn(Book.builder().id((long) 1).title("A Cabana").author("Pâmela").isbn("034").build());

        // execução
        Book savedBook = service.save(book);

        // verificação
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getAuthor()).isEqualTo("Pâmela");
        assertThat(savedBook.getTitle()).isEqualTo("A Cabana");
        assertThat(savedBook.getIsbn()).isEqualTo("034");
    }

    private Book createNewBook() {
        return Book.builder().title("A Cabana").author("Pâmela").isbn("034").build();
    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao tentar cadastrar livro com isbn já utilizado por outro")
    public void shouldNotSaveBookWithDuplicatedISBN() {
        // cenário
        Book book = createNewBook();
        String messageError = "Isbn já cadastrado!";
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        // execução
        Throwable ex = Assertions.catchThrowable(() -> service.save(book));

        // verificações
        assertThat(ex).isInstanceOf(BusinessException.class).hasMessage(messageError);
        Mockito.verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve obter o livro pelo id")
    public void getByIdTest() {
        // cenário
        Long id = 1L;

        Book book = createNewBook();
        book.setId(id);
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));

        // execução
        Optional<Book> foundBook =  service.getById(id);

        // verificações
        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(id);
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
    }

    @Test
    @DisplayName("Deve retornar vazio quando livro mão existir na base")
    public void bookNotFoundByIdTest() {
        // cenário
        Long id = 1L;

        Mockito.when(repository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        // execução
        Optional<Book> book =  service.getById(id);

        // verificações
        assertThat(book.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Deve deletar um livro com sucesso")
    public void deleteBookTest() {
        // cenário
        Book book = createNewBook();
        book.setId(1L);

        // execução
        assertDoesNotThrow(() -> service.delete(book));

        // verificações
        Mockito.verify(repository, Mockito.times(1)).delete(book);
    }

    @Test
    @DisplayName("Deve retornar exceção ao tentar deletar um livro sem id")
    public void shouldNotDeleteBookTest() {
        // cenário
        Book book = createNewBook();

        // execução
        assertThrows(IllegalArgumentException.class, () -> service.delete(book));

        // verificações
        Mockito.verify(repository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Deve atualizar um livro com sucesso")
    public void updateBookTest() {
        // cenário
        Long id = 1l;
        Book updatingBook = Book.builder().id(id).build();

        Book updatedBook = createNewBook();
        updatedBook.setId(id);

        // execução
        Mockito.when(repository.save(updatingBook)).thenReturn(updatedBook);
        Book book = service.update(updatingBook);

        // verificações
        assertThat(updatedBook.getId()).isEqualTo(updatedBook.getId());
        assertThat(updatedBook.getAuthor()).isEqualTo(updatedBook.getAuthor());
        assertThat(updatedBook.getTitle()).isEqualTo(updatedBook.getTitle());
        assertThat(updatedBook.getIsbn()).isEqualTo(updatedBook.getIsbn());
    }

    @Test
    @DisplayName("Deve retornar exceção ao tentar atualizar um livro sem id")
    public void shouldNotUpdateBookTest() {
        // cenário
        Book book = createNewBook();

        // execução
        assertThrows(IllegalArgumentException.class, () -> service.update(book));

        // verificações
        Mockito.verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve filtrar livro pelas propriedades")
    public void findBookTest() {

        //cenário
        Book book = createNewBook();
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Book> list = Arrays.asList(book);
        Page<Book> page = new PageImpl<Book>(list, pageRequest, 1);
        Mockito.when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class))).thenReturn(page);

        // execução
        Page<Book> result = service.find(book, pageRequest);

        // verificações
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(list);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);

    }

    @Test
    @DisplayName("Deve obter um livro pelo Isbn")
    public void getBookByIsbnTest() {

        String isbn = "123";
        Mockito.when(repository.findByIsbn(isbn)).thenReturn(Optional.of(Book.builder().id(1l).isbn(isbn).build()));

        Optional<Book> book = service.getBookByIsbn(isbn);

        assertThat(book.isPresent()).isTrue();
        assertThat(book.get().getId()).isEqualTo(1l);
        assertThat(book.get().getIsbn()).isEqualTo(isbn);

        Mockito.verify(repository, Mockito.times(1)).findByIsbn(isbn);
    }
}
