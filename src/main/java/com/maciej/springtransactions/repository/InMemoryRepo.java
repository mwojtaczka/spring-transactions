package com.maciej.springtransactions.repository;

import com.maciej.springtransactions.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InMemoryRepo extends JpaRepository<Person, String> {

    List<Person> findBySurname(String surname);


}
