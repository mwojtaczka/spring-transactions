package com.maciej.springtransactions.service;

import com.maciej.springtransactions.exception.MyException;
import com.maciej.springtransactions.model.Person;
import org.springframework.stereotype.Service;

@Service
public class ServiceB {

    private final InMemoryRepo repo;

    public ServiceB(InMemoryRepo repo) {
        this.repo = repo;
    }

    public void add(Person person) {
        repo.save(person);
    }

    public void failWithMyException() throws MyException {
        throw new MyException();
    }

    public void failWithRuntime() {
        throw new RuntimeException();
    }
}
