package com.maciej.springtransactions.service;

import com.maciej.springtransactions.exception.MyException;
import com.maciej.springtransactions.model.Person;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MyAnnotatedService {


    private final ServiceA serviceA;
    private final ServiceB serviceB;

    public MyAnnotatedService(ServiceA serviceA, ServiceB serviceB) {
        this.serviceA = serviceA;
        this.serviceB = serviceB;
    }

    @Transactional
    public void transactionalMethod() {
        Person personA = Person.builder()
                .name("A").build();
        Person personB = Person.builder()
                .name("B").build();

        serviceA.add(personA);
        serviceB.add(personB);
    }

    @Transactional
    public void transactionalMethod_whereOneServiceFailsWithRuntime() {
        Person personA = Person.builder()
                .name("A").build();
        serviceA.add(personA);
        serviceB.failWithRuntime();
    }

    @Transactional
    public void transactionalMethod_whereOneServiceFailsWithMyExc() throws MyException {
        Person personA = Person.builder()
                .name("A").build();
        serviceA.add(personA);
        serviceB.failWithMyException();
    }

    @Transactional(rollbackFor = MyException.class)
    public void transactionalMethod_whereOneServiceFailsWithMyExc_andRollbackForIsMyException() throws MyException {
        Person personA = Person.builder()
                .name("A").build();
        serviceA.add(personA);
        serviceB.failWithMyException();
    }

    @Transactional
    public void transactionalMethod_whereOneServiceFailsWithMyExcButCaught() {
        Person personA = Person.builder()
                .name("A").build();
        try {
            serviceA.add(personA);
            serviceB.failWithMyException();
        } catch (MyException ignored) {
        }
    }

    @Transactional
    public void transactionalMethod_whereOneServiceFailsWithMyExcButCaught_andExceptionRethrowToRuntime() {
        Person personA = Person.builder()
                .name("A").build();
        try {
            serviceA.add(personA);
            serviceB.failWithMyException();
        } catch (MyException ignored) {
            throw new RuntimeException();
        }
    }

    public void nonTransactionalWrapperForTransactionalMethodWhereOneFailsWithRuntime() throws MyException {
        transactionalMethod_whereOneServiceFailsWithRuntime();
    }

    @Transactional
    public void transactionalWrapperForTransactionalMethodWhereOneFailsWithRuntime() {
        transactionalMethod_whereOneServiceFailsWithRuntime();
    }

    @Transactional
    public void transactionalWrapperForTransactionalMethodWhereOneFailsWithMyExc() throws MyException {
        transactionalMethod_whereOneServiceFailsWithMyExc();
    }
}
