package com.maciej.springtransactions.service.propagation;

import com.maciej.springtransactions.model.Person;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MyNestedAnnotatedService {

    private final ServiceA serviceA;
    private final ServiceB serviceB;

    public MyNestedAnnotatedService(ServiceA serviceA, ServiceB serviceB) {
        this.serviceA = serviceA;
        this.serviceB = serviceB;
    }

    @Transactional
    public void transactionalSucceedingMethod() {
        Person personA = Person.builder()
                .name("NA").build();

        serviceA.add(personA);
    }

    @Transactional(propagation = Propagation.REQUIRED) //this is Default one
    public void transactionalMethodWhenOneFails_andPropagationRequired() {
        Person personA = Person.builder()
                .name("NA").build();
        serviceA.add(personA);
        serviceB.failWithRuntime();
    }

    @Transactional(propagation = Propagation.NESTED)
    public void transactionalMethodWhenOneFails_andPropagationNested() {
        Person personA = Person.builder()
                .name("NA").build();
        serviceA.add(personA);
        serviceB.failWithRuntime();
    }

    @Transactional(propagation = Propagation.NESTED)
    public void transactionalSucceedingMethod_andPropagationNested() {
        Person personA = Person.builder()
                .name("NA").build();

        serviceA.add(personA);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void transactionalSucceedingMethod_andPropagationRequiredNew() {
        Person personA = Person.builder()
                .name("NA").build();

        serviceA.add(personA);
    }

}
