package com.maciej.springtransactions.service.propagation;

import com.maciej.springtransactions.exception.MyException;
import com.maciej.springtransactions.model.Person;
import com.maciej.springtransactions.repository.InMemoryRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class MyAnnotatedServiceTest {

    @Autowired
    private MyAnnotatedService myAnnotatedService;

    @Autowired
    private InMemoryRepo repo;

    @BeforeEach
    void cleanDb() {
        repo.deleteAll();
    }

    @Test
    void shouldCommitTransaction() {

        myAnnotatedService.transactionalMethod();

        assertThat(repo.findAll()).hasSize(2);
    }

    @Test
    void shouldRollbackTransaction_whenOneServiceFailsWithRuntime() {

        assertThrows(RuntimeException.class, () -> myAnnotatedService.transactionalMethod_whereOneServiceFailsWithRuntime());

        assertThat(repo.findAll()).isEmpty();
    }

    @Test
        //it is so because rollbacks by default are triggered only for RuntimeExceptions
    void shouldCommitTransaction_whenOneServiceFailsWithMyException() {

        assertThrows(MyException.class, () -> myAnnotatedService.transactionalMethod_whereOneServiceFailsWithMyExc());

        assertThat(repo.findAll()).hasSize(1);
    }

    @Test
        //it is so because rollbacks by default are triggered only for RuntimeExceptions
    void shouldRollbackTransaction_whenOneServiceFailsWithMyException_butRollBackForIsSetForMyException() {

        assertThrows(MyException.class,
                () -> myAnnotatedService.transactionalMethod_whereOneServiceFailsWithMyExc_andRollbackForIsMyException());

        assertThat(repo.findAll()).isEmpty();
    }

    @Test
    void shouldCommitTransaction_whenOneServiceFailsWithMyExc_butExceptionIsBeingCaughtWithinTransactionalMethod() {

        myAnnotatedService.transactionalMethod_whereOneServiceFailsWithMyExcButCaught();

        final Iterable<Person> all = repo.findAll();
        assertThat(all).hasSize(1);
        Person savedPerson = all.iterator().next();
        assertThat(savedPerson.getName()).isEqualTo("A");
    }

    @Test
    void shouldRollbackTransaction_whenOneServiceFailsWithMyExc_butExceptionIsBeingCaughtWithinTransactionalMethod_andRethrowToRuntime() {

        assertThrows(RuntimeException.class,
                () -> myAnnotatedService.transactionalMethod_whereOneServiceFailsWithMyExcButCaught_andExceptionRethrowToRuntime());

        assertThat(repo.findAll()).isEmpty();
    }

    @Test
    void shouldBeNoTransactionAtAll_whenOneServiceFails_butTransactionalMethodIsBeingCalledFromNonTransactionalOne() {

        assertThrows(RuntimeException.class, () -> myAnnotatedService.nonTransactionalWrapperForTransactionalMethodWhereOneFailsWithRuntime());

        final Iterable<Person> all = repo.findAll();
        assertThat(all).hasSize(1);
        Person savedPerson = all.iterator().next();
        assertThat(savedPerson.getName()).isEqualTo("A");
    }

    @Test
        //in that case second @Transactional is ignored and we have just one transaction from first method so technically
        // this case equal to shouldRollbackTransaction_whenOneServiceFailsWithRuntime
    void shouldRollbackTransaction_whenOneServiceFailsWithRuntime_andTransactionalMethodIsBeingCalledFromTransactionalOne() {

        assertThrows(RuntimeException.class, () -> myAnnotatedService.transactionalWrapperForTransactionalMethodWhereOneFailsWithRuntime());

        assertThat(repo.findAll()).isEmpty();
    }

    @Test
        //in that case second @Transactional is ignored and we have just one transaction from first method so technically
        // this case equal to shouldCommitTransaction_whenOneServiceFailsWithMyException
    void shouldCommitTransaction_whenOneServiceFailsWithMyException_andTransactionalMethodIsBeingCalledFromTransactionalOne() {

        assertThrows(MyException.class, () -> myAnnotatedService.transactionalWrapperForTransactionalMethodWhereOneFailsWithMyExc());

        assertThat(repo.findAll()).hasSize(1);
    }

}