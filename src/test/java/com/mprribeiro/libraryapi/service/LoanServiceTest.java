package com.mprribeiro.libraryapi.service;

import com.mprribeiro.libraryapi.api.dto.LoanFilterDTO;
import com.mprribeiro.libraryapi.api.exception.BusinessException;
import com.mprribeiro.libraryapi.api.model.entity.Book;
import com.mprribeiro.libraryapi.api.model.entity.Loan;
import com.mprribeiro.libraryapi.api.model.repository.LoanRepository;
import com.mprribeiro.libraryapi.api.service.LoanService;
import com.mprribeiro.libraryapi.api.service.impl.LoanServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    LoanService service;

    @MockBean
    LoanRepository repository;

    @BeforeEach
    public void setUp() {
        this.service = new LoanServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um empréstimo")
    public void saveLoanTest() {
        // cenário
        Loan savingLoan = createNewLoan();
        Loan savedLoan = createNewLoan();
        savedLoan.setId(1l);
        Mockito.when(repository.save(Mockito.any(Loan.class))).thenReturn(savedLoan);

        // execução
        Loan loan = service.save(savingLoan);

        // verificação
        assertThat(loan.getId()).isEqualTo(savedLoan.getId());
        assertThat(loan.getBook()).isEqualTo(savedLoan.getBook());
        assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
    }

    @Test
    @DisplayName("Deve lanãr erro de negócio ao tentar salvar um empréstimo com livro já emprestado")
    public void shouldNotSaveLoanTest() {
        // cenário
        Loan loan = createNewLoan();
        String messageError = "Livro já emprestado!";
        Mockito.when(repository.existsByBookAndNotReturned(loan.getBook())).thenReturn(true);

        // execução
        Throwable ex = Assertions.catchThrowable(() -> service.save(loan));

        // verificação
        assertThat(ex).isInstanceOf(BusinessException.class).hasMessage(messageError);;
        Mockito.verify(repository, Mockito.never()).save(loan);
    }

    private Loan createNewLoan() {
        Book book = Book.builder().title("A Cabana").author("Pâmela").isbn("034").build();
        return Loan.builder().book(book).customer("Ciclano").loanDate(LocalDate.now()).build();
    }

    @Test
    @DisplayName("Deve obter as informações de um empréstimo pelo ID")
    public void getLoanDetailsTest() {

        // cenário
        Long id = 1l;

        Loan loan = createNewLoan();
        loan.setId(id);

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(loan));

        // execução
        Optional<Loan> resultLoan = service.getById(id);

        // verificação
        assertThat(resultLoan.isPresent()).isTrue();
        assertThat(resultLoan.get().getId()).isEqualTo(loan.getId());
        assertThat(resultLoan.get().getLoanDate()).isEqualTo(loan.getLoanDate());
        assertThat(resultLoan.get().getCustomer()).isEqualTo(loan.getCustomer());
        assertThat(resultLoan.get().getBook()).isEqualTo(loan.getBook());
        assertThat(resultLoan.get().getReturned()).isEqualTo(loan.getReturned());

        Mockito.verify(repository).findById(id);
    }

    @Test
    @DisplayName("Deve atualizar um empréstimo")
    public void updateLoanTest() {

        // cenário
        Loan loan = createNewLoan();
        loan.setId(1l);
        loan.setReturned(true);

        Mockito.when(repository.save(loan)).thenReturn(loan);

        // execução
        Loan updatedLoan = service.update(loan);

        // verificação
        assertThat(updatedLoan.getReturned()).isTrue();
        Mockito.verify(repository).save(loan);
    }

    @Test
    @DisplayName("Deve filtrar empréstimo pelas propriedades")
    public void findLoanTest() {

        //cenário
        LoanFilterDTO dto = LoanFilterDTO.builder().customer("John").isbn("456").build();

        Loan loan = createNewLoan();
        loan.setId(1l);
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Loan> list = Arrays.asList(loan);

        Page<Loan> page = new PageImpl<Loan>(list, pageRequest, list.size());
        Mockito.when(repository.findByBookIsbnOrCustomer(Mockito.anyString(), Mockito.anyString(), Mockito.any(PageRequest.class))).thenReturn(page);

        // execução
        Page<Loan> result = service.find(dto, pageRequest);

        // verificações
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(list);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);

    }
}
