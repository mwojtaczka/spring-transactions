package com.maciej.springtransactions.service.propagation;

import com.maciej.springtransactions.model.Person;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MyAnnotatedService_Propagation {


    private final ServiceA serviceA;
    private final ServiceB serviceB;
    private final MyNestedAnnotatedService nestedAnnotatedService;

    public MyAnnotatedService_Propagation(ServiceA serviceA, ServiceB serviceB, MyNestedAnnotatedService nestedAnnotatedService) {
        this.serviceA = serviceA;
        this.serviceB = serviceB;
        this.nestedAnnotatedService = nestedAnnotatedService;
    }

    @Transactional
    public void transactionalMethodWithNestedTransactionalMethodWhichHasPropagationRequired() {
        Person personA = Person.builder()
                .name("A").build();
        Person personB = Person.builder()
                .name("B").build();

        serviceA.add(personA);
        serviceB.add(personB);

        try {
            nestedAnnotatedService.transactionalSucceedingMethod();
        } catch (RuntimeException ignored) {
        }
    }


    @Transactional
    public void transactionalMethodWithNestedFailingTransactionalMethodWhichHasPropagationRequired() {
        Person personA = Person.builder()
                .name("A").build();
        Person personB = Person.builder()
                .name("B").build();

        serviceA.add(personA);
        serviceB.add(personB);

        try {
            nestedAnnotatedService.transactionalMethodWhenOneFails_andPropagationRequired();
        } catch (RuntimeException ignored) {
        }
    }

    public void nonTransactionalMethodWithNestedFailingTransactionalMethodWhichHasPropagationRequired() {
        Person personA = Person.builder()
                .name("A").build();
        Person personB = Person.builder()
                .name("B").build();

        serviceA.add(personA);
        serviceB.add(personB);

        try {
            nestedAnnotatedService.transactionalMethodWhenOneFails_andPropagationRequired();
        } catch (RuntimeException ignored) {
        }
    }

    @Transactional
    public void transactionalMethodWithNestedFailingTransactionalMethodWhichHasPropagationNested() {
        Person personA = Person.builder()
                .name("A").build();
        Person personB = Person.builder()
                .name("B").build();

        serviceA.add(personA);
        serviceB.add(personB);

        try {
            nestedAnnotatedService.transactionalMethodWhenOneFails_andPropagationNested();
        } catch (RuntimeException ignored) {
        }
    }

    @Transactional
    public void failingTransactionalMethodWithNestedSucceedingTransactionalMethodWhichHasPropagationNested() {
        Person personA = Person.builder()
                .name("A").build();
        Person personB = Person.builder()
                .name("B").build();

        serviceA.add(personA);
        serviceB.add(personB);

        try {
            nestedAnnotatedService.transactionalSucceedingMethod_andPropagationNested();
        } catch (RuntimeException ignored) {
        }
        serviceB.failWithRuntime();
    }

    @Transactional
    public void failingTransactionalMethodWithNestedSucceedingTransactionalMethodWhichHasPropagationRequiredNew() {
        Person personA = Person.builder()
                .name("A").build();
        Person personB = Person.builder()
                .name("B").build();

        serviceA.add(personA);
        serviceB.add(personB);

        try {
            nestedAnnotatedService.transactionalSucceedingMethod_andPropagationRequiredNew();
        } catch (RuntimeException ignored) {
        }
        serviceB.failWithRuntime();
    }






}
