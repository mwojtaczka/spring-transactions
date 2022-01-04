package com.maciej.springtransactions.repository;

import com.maciej.springtransactions.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public interface InMemoryRepo extends JpaRepository<Person, String> {

    List<Person> findBySurname(String surname);


}
