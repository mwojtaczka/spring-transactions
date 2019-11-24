package com.maciej.springtransactions.service;

import com.maciej.springtransactions.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class MyAnnotatedServicePropagationTest {

    @Autowired
    private MyAnnotatedService_Propagation myAnnotatedServicePropagation;

    @Autowired
    private ServiceA serviceA;

    @Autowired
    private ServiceB serviceB;

    @Autowired
    private InMemoryRepo repo;

    @BeforeEach
    void cleanDb() {
        repo.deleteAll();
    }


    @Test
        //both method outer and nested share same transaction
    void shouldCommitOneCommonTransactionAndAllThreeInDb_whenNestedTransactionalMethodSucceed_andNestedHasPropagationRequired() {

        myAnnotatedServicePropagation.transactionalMethodWithNestedTransactionalMethodWhichHasPropagationRequired();

        assertThat(repo.findAll()).hasSize(3);
    }

    @Test
        //both method outer and nested share same transaction
    void shouldRollbackOneCommonTransaction_whenInNestedTransactionalMethodOneServiceFails_andNestedHasPropagationRequired() {

        assertThrows(RuntimeException.class, () -> myAnnotatedServicePropagation.transactionalMethodWithNestedFailingTransactionalMethodWhichHasPropagationRequired());

        assertThat(repo.findAll()).isEmpty();
    }

    @Test
        //outer method is not transactional so only nested is within transaction
    void shouldRollbackOneNestedTransactionAndTwoInDb_whenInNestedTransactionalMethodOneServiceFails_andNestedHasPropagationRequired() {

        myAnnotatedServicePropagation.nonTransactionalMethodWithNestedFailingTransactionalMethodWhichHasPropagationRequired();

        final Iterable<Person> all = repo.findAll();
        assertThat(all).hasSize(2);
        assertThat(all.iterator().next().getName()).matches("A|B");
        assertThat(all.iterator().next().getName()).matches("A|B");
    }

    @Test
        //both methods are transactional but nested one has propagation as NESTED so both have its own transaction
        // so nested doesn't rollback the outer but outer rollbacks the nested
    void shouldRollbackOnlyNestedTransactionAndCommitOuterTransactionAndTwoInDb_whenInNestedTransactionalMethodOneServiceFails_andNestedHasPropagationNested() {

        myAnnotatedServicePropagation.transactionalMethodWithNestedFailingTransactionalMethodWhichHasPropagationNested();

        final Iterable<Person> all = repo.findAll();
        assertThat(all).hasSize(2);
        assertThat(all.iterator().next().getName()).matches("A|B");
        assertThat(all.iterator().next().getName()).matches("A|B");
    }

    @Test
        //in this case outer rollbacks nested
    void shouldRollbackBothTransactions_whenInOuterTransactionalMethodOneServiceFails_andNestedHasPropagationNested() {

        assertThrows(RuntimeException.class, () -> myAnnotatedServicePropagation.failingTransactionalMethodWithNestedSucceedingTransactionalMethodWhichHasPropagationNested());

        assertThat(repo.findAll()).isEmpty();
    }

    @Test
        //both methods are transactional but nested one has propagation as NESTED so both have its own independent transaction
        // so exception in one doesn't rollback another
    void shouldRollbackOnlyOuterTransactionAndCommitNestedTransactionAndOneInDb_whenInOuterTransactionalMethodOneServiceFails_andNestedHasPropagationRequiredNew() {

        assertThrows(RuntimeException.class, () -> myAnnotatedServicePropagation.failingTransactionalMethodWithNestedSucceedingTransactionalMethodWhichHasPropagationRequiredNew());

        final Iterable<Person> all = repo.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.iterator().next().getName()).matches("NA");
    }


}