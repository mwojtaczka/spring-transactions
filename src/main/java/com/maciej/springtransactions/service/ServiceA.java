package com.maciej.springtransactions.service;

import com.maciej.springtransactions.model.Person;
import org.springframework.stereotype.Service;

@Service
public class ServiceA {

    private final InMemoryRepo repo;

    public ServiceA(InMemoryRepo repo) {
        this.repo = repo;
    }

    public void add(Person person) {
        repo.save(person);
    }

}
