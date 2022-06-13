package com.example.loanShark.repository;

import com.example.loanShark.model.LoanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanTypeRepository extends JpaRepository<LoanType, Long> {
    Optional<LoanType> findById(Long id);
}
