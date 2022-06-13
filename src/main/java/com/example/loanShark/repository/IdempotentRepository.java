package com.example.loanShark.repository;

import com.example.loanShark.model.IdempotentKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdempotentRepository extends JpaRepository<IdempotentKey, Long> {
    Optional<IdempotentKey> findByUniqueKey(String uniqueKey);
}
