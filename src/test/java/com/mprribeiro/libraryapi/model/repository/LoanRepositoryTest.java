package com.mprribeiro.libraryapi.model.repository;

import com.mprribeiro.libraryapi.api.model.entity.Book;
import com.mprribeiro.libraryapi.api.model.entity.Loan;
import com.mprribeiro.libraryapi.api.model.repository.LoanRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    private LoanRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve verificar se o livro está emprestado")
    public void existsByBookAndNotReturnedTest() {

        // cenario
        Loan loan = createAndPersistLoan(LocalDate.now());

        // execução
        boolean exists = repository.existsByBookAndNotReturned(loan.getBook());

        Assertions.assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve buscar empréstimo pelo isbn do livro ou customer")
    public void findByBookIsbnOrCustomerTest() {

        // cenario
        Loan loan = createAndPersistLoan(LocalDate.now());

        // execução
        Page<Loan> result =  repository.findByBookIsbnOrCustomer("034", "Ciclano", PageRequest.of(0, 10));

        Assertions.assertThat(result.getContent()).hasSize(1);
        Assertions.assertThat(result.getContent()).contains(loan);
        Assertions.assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        Assertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        Assertions.assertThat(result.getTotalElements()).isEqualTo(1);
    }

    private Loan createAndPersistLoan(LocalDate loanDate) {
        Book book = Book.builder().title("A Cabana").author("Pâmela").isbn("034").build();
        entityManager.persist(book);
        Loan loan = Loan.builder().customer("Ciclano").loanDate(loanDate).build();
        loan.setBook(book);
        entityManager.persist(loan);
        return loan;
    }

    @Test
    @DisplayName("Deve obter empréstimos atrasados")
    public void findByLoanDateLessThanAndNotReturnedTest() {

        // cenario
        Loan loan = createAndPersistLoan(LocalDate.now().minusDays(5));

        List<Loan> result = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        Assertions.assertThat(result).hasSize(1).contains(loan);
    }

    @Test
    @DisplayName("Não deve obter empréstimos atrasados")
    public void notFindByLoanDateLessThanAndNotReturnedTest() {

        // cenario
        Loan loan = createAndPersistLoan(LocalDate.now());

        List<Loan> result = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        Assertions.assertThat(result).isEmpty();
    }
}
