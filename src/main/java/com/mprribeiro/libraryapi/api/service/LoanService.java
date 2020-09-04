package com.mprribeiro.libraryapi.api.service;

import com.mprribeiro.libraryapi.api.dto.LoanFilterDTO;
import com.mprribeiro.libraryapi.api.model.entity.Book;
import com.mprribeiro.libraryapi.api.model.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

public interface LoanService {

    Loan save(Loan loan);

    Optional<Loan> getById(Long id);

    Loan update(Loan loan);

    Page<Loan> find(LoanFilterDTO filter, Pageable pageable);

    Page<Loan> getLoansByBook(Book book, Pageable pageable);

    List<Loan> getAllLateLoans();
}
