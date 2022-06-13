package com.example.loanShark.services;

import com.example.loanShark.exceptions.IdempotentKeyNotUnique;
import com.example.loanShark.model.IdempotentKey;
import com.example.loanShark.repository.IdempotentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class IdempotentService {
    public static final String REQUEST_HAS_BEEN_PROCESSED = "Request has been processed already!";
    private IdempotentRepository idempotentRepository;

    public Boolean validateIdempotentRequest(String uniqueKey) {
        if (idempotentRepository.findByUniqueKey(uniqueKey).isPresent()) {
            throw new IdempotentKeyNotUnique(REQUEST_HAS_BEEN_PROCESSED, "d1b83115-9c98-4d78-a625-e415a4ab0a34");
        }
        IdempotentKey idempotentKey = new IdempotentKey();
        idempotentKey.setUniqueKey(uniqueKey);
        idempotentRepository.save(idempotentKey);

        return true;
    }
}
