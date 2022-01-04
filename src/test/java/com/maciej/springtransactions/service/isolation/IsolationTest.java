package com.maciej.springtransactions.service.isolation;

import com.maciej.springtransactions.model.Person;
import com.maciej.springtransactions.repository.InMemoryRepo;
import org.hibernate.exception.LockAcquisitionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class IsolationTest {

    @Autowired
    private InMemoryRepo repo;

    @Autowired
    private SlowService slowService;

    @Autowired
    private QuickService quickService;

    @BeforeEach
    void setup() {
        Person person = Person.builder().name("John").surname("Smith").money(BigDecimal.ZERO).build();
        repo.save(person);
    }

    @AfterEach
    void cleanup() {
        repo.deleteAll();
    }

    @Test
    @DisplayName("should read uncommitted data")
    void dirtyRead() {
        final CompletableFuture<Void> future = runAsync(() -> slowService.depositMoney_andFail("John", BigDecimal.TEN));
        sleep(100);
        final Person dirtyJohn = slowService.getById_readUncommitted("John").orElseThrow();

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        Person actualJohn = repo.findById("John").orElseThrow();
        assertThat(dirtyJohn.getMoney()).usingComparator(BigDecimal::compareTo).isEqualTo(BigDecimal.TEN);
        assertThat(actualJohn.getMoney()).usingComparator(BigDecimal::compareTo).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("should read uncommitted data")
    void read() {
        final CompletableFuture<Void> future = runAsync(() -> slowService.depositMoney_andFail("John", BigDecimal.TEN));
        sleep(100);
        final Person dirtyJohn = slowService.getById_readCommitted("John").orElseThrow();

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        Person actualJohn = repo.findById("John").orElseThrow();
        assertThat(dirtyJohn.getMoney()).usingComparator(BigDecimal::compareTo).isEqualTo(BigDecimal.ZERO);
        assertThat(actualJohn.getMoney()).usingComparator(BigDecimal::compareTo).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("should lose update from QuickService")
    void lostUpdate() throws ExecutionException, InterruptedException {

        final CompletableFuture<Void> future = runAsync(() -> slowService.depositMoney("John", BigDecimal.TEN));
        quickService.depositMoney("John", BigDecimal.ONE);
        future.get();

        final Person john = repo.findById("John").orElseThrow();

        assertThat(john.getMoney()).usingComparator(BigDecimal::compareTo).isEqualTo(BigDecimal.TEN);
    }

    @Test
//    @Disabled("doesn't work, at least with H2")
    @DisplayName("should throw exception when resource is locked by other transaction")
    void update() throws ExecutionException, InterruptedException {

        final CompletableFuture<Void> future =
                runAsync(() -> slowService.depositMoney_transactional("John", BigDecimal.TEN));
        sleep(100);

        assertThatThrownBy(() -> quickService.depositMoney_transactional("John", BigDecimal.ONE))
                .isInstanceOf(CannotAcquireLockException.class);

        future.get();

        final Person john = repo.findById("John").orElseThrow();

        assertThat(john.getMoney()).usingComparator(BigDecimal::compareTo).isEqualTo(BigDecimal.valueOf(10));
    }

    @Test
    @DisplayName("should return 2 different results when retrievals are not in transaction")
    void unrepeatableReads() throws ExecutionException, InterruptedException {

        final CompletableFuture<List<Person>> twoJohnsFuture = supplyAsync(() -> slowService.getPersonByIdTwoTimes("John"));
        sleep(100);
        quickService.updateSurname("John", "Wick");

        final List<Person> twoJohns = twoJohnsFuture.get();
        final Person firstRetrieval = twoJohns.get(0);
        final Person secondRetrieval = twoJohns.get(1);
        final Person actual = repo.findById("John").orElseThrow();

        assertThat(firstRetrieval).isNotEqualTo(secondRetrieval);
        assertThat(actual.getSurname()).isEqualTo("Wick");
    }

    @Test
    @DisplayName("should return 2 same results when retrievals are in transaction")
    void repeatableReads() throws ExecutionException, InterruptedException {

        final CompletableFuture<List<Person>> twoJohnsFuture =
                supplyAsync(() -> slowService.getPersonByIdTwoTimes_transactional("John"));
        sleep(100);
        quickService.updateSurname("John", "Wick");

        final List<Person> twoJohns = twoJohnsFuture.get();
        final Person firstRetrieval = twoJohns.get(0);
        final Person secondRetrieval = twoJohns.get(1);
        final Person actual = repo.findById("John").orElseThrow();

        assertThat(firstRetrieval.getSurname()).isEqualTo("Smith");
        assertThat(firstRetrieval).isEqualTo(secondRetrieval);
        assertThat(actual.getSurname()).isEqualTo("Wick");
    }

    @Test
    @DisplayName("should return 2 different result sets when retrievals are in read_committed transaction")
    void phantomReads() throws ExecutionException, InterruptedException {

        final CompletableFuture<List<List<Person>>> twoResultsFuture =
                supplyAsync(() -> slowService.getAllWithSurname("Smith"));
        sleep(500);
        quickService.addNewPerson("Jennifer", "Smith");

        final List<List<Person>> twoResultSets = twoResultsFuture.get();

        assertThat(twoResultSets.get(0).size()).isEqualTo(1);
        assertThat(twoResultSets.get(0).size()).isNotEqualTo(twoResultSets.get(1).size());
    }

    @Test
    @DisplayName("should return 2 same result sets when retrievals are not in transaction")
    void noPhantomReads() throws ExecutionException, InterruptedException {

        final CompletableFuture<List<List<Person>>> twoResultsFuture =
                supplyAsync(() -> slowService.getAllWithSurname_transactional("Smith"));
        sleep(500);
        quickService.addNewPerson("Jennifer", "Smith");

        final List<List<Person>> twoResultSets = twoResultsFuture.get();

        assertThat(twoResultSets.get(0).size()).isEqualTo(1);
        assertThat(twoResultSets.get(0).size()).isEqualTo(twoResultSets.get(1).size());
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
