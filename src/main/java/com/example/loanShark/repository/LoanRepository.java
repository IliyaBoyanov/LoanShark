package com.example.loanShark.repository;

import com.example.loanShark.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Loan> findWithLockingById(Long id);
    List<Loan> findAllByUserId(Long id);
}
