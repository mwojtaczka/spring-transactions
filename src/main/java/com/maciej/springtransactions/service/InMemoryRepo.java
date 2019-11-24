package com.maciej.springtransactions.service;

import com.maciej.springtransactions.model.Person;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Repository
public interface InMemoryRepo extends CrudRepository<Person, String> {


}
