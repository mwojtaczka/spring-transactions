package com.maciej.springtransactions.service.isolation;

import com.maciej.springtransactions.model.Person;
import com.maciej.springtransactions.repository.InMemoryRepo;
import org.hibernate.Session;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SlowService {

    private final InMemoryRepo repo;

    public SlowService(InMemoryRepo repo) {
        this.repo = repo;
    }

    public void depositMoney(String id, BigDecimal deposit) {
        final Person person = repo.findById(id).orElseThrow();
        final BigDecimal current = person.getMoney();
        person.setMoney(current.add(deposit));
        sleep(1000);
        repo.saveAndFlush(person);
    }

    @Transactional
    public void depositMoney_transactional(String id, BigDecimal deposit) {
        final Person person = repo.findById(id).orElseThrow();
        final BigDecimal current = person.getMoney();
        person.setMoney(current.add(deposit));
        sleep(1000);
        repo.saveAndFlush(person);
    }

    public List<Person> getPersonByIdTwoTimes(String id) {
        System.out.println("slow");
        final Person firstRetrieval = repo.findById(id).orElseThrow();

        sleep(1000);
        System.out.println("slow");

        final Person secondRetrieval = repo.findById(id).orElseThrow();

        return List.of(firstRetrieval, secondRetrieval);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<Person> getPersonByIdTwoTimes_transactional(String id) {
        final Person firstRetrieval = repo.findById(id).orElseThrow();

        sleep(1000);

        final Person secondRetrieval = repo.findById(id).orElseThrow();

        return List.of(firstRetrieval, secondRetrieval);
    }

    public List<List<Person>> getAllWithSurname(String surname) {
        final List<Person> bySurname = repo.findBySurname(surname);
        System.out.println(repo.findAll());
        sleep(1000);

        final List<Person> bySurname2 = repo.findBySurname(surname);

        return List.of(bySurname, bySurname2);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public List<List<Person>> getAllWithSurname_transactional(String surname) {
        final List<Person> bySurname = repo.findBySurname(surname);
        System.out.println(repo.findAll());
        sleep(1000);

        final List<Person> bySurname2 = repo.findBySurname(surname);

        return List.of(bySurname, bySurname2);
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
