package com.maciej.springtransactions.service.isolation;

import com.maciej.springtransactions.model.Person;
import com.maciej.springtransactions.repository.InMemoryRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class QuickService {

    private final InMemoryRepo repo;

    public QuickService(InMemoryRepo repo) {
        this.repo = repo;
    }

    public void depositMoney(String id, BigDecimal deposit) {
        final Person person = repo.findById(id).orElseThrow();
        final BigDecimal current = person.getMoney();
        person.setMoney(current.add(deposit));
        repo.saveAndFlush(person);
    }

    @Transactional
    public void depositMoney_transactional(String id, BigDecimal deposit) {
        final Person person = repo.findById(id).orElseThrow();
        final BigDecimal current = person.getMoney();
        person.setMoney(current.add(deposit));
        repo.saveAndFlush(person);
    }

    public void updateSurname(String id, String surname) {
        final Person person = repo.findById(id).orElseThrow();
        person.setSurname(surname);
        repo.saveAndFlush(person);
    }

    public void addNewPerson(String name, String surname) {
        final Person newPerson = Person.builder().name(name).surname(surname).build();
        repo.saveAndFlush(newPerson);
    }

}
