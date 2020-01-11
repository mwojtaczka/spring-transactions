package com.maciej.springtransactions.service.isolation;

import com.maciej.springtransactions.model.Person;
import com.maciej.springtransactions.repository.InMemoryRepo;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.assertj.core.api.Assertions.assertThat;

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
    @DisplayName("should lost update from QuickService")
    void lostUpdate() throws ExecutionException, InterruptedException {

        final CompletableFuture<Void> future = runAsync(() -> slowService.depositMoney("John", BigDecimal.TEN));
        quickService.depositMoney("John", BigDecimal.ONE);
        future.get();

        final Person john = repo.findById("John").orElseThrow();

        assertThat(john.getMoney()).usingComparator(BigDecimal::compareTo).isEqualTo(BigDecimal.TEN);
    }

    @Test
    @Disabled("doesn't work, at least with H2")
    @DisplayName("should not lost update from QuickService")
    void update() throws ExecutionException, InterruptedException {

        final CompletableFuture<Void> future =
                runAsync(() -> slowService.depositMoney_transactional("John", BigDecimal.TEN));
        quickService.depositMoney_transactional("John", BigDecimal.ONE);
        future.get();

        final Person john = repo.findById("John").orElseThrow();

        assertThat(john.getMoney()).usingComparator(BigDecimal::compareTo).isEqualTo(BigDecimal.valueOf(11));
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
    @DisplayName("should return 2 different result sets when retrievals are not in transaction")
    void phantomReads() throws ExecutionException, InterruptedException {

        final CompletableFuture<List<List<Person>>> twoResultsFuture =
                supplyAsync(() -> slowService.getAllWithSurname("Smith"));
        sleep(500);
        quickService.addNewPerson("Jennifer", "Smith");

        final List<List<Person>> twoResultSets = twoResultsFuture.get();

        assertThat(twoResultSets.get(0).size()).isEqualTo(1);
        assertThat(twoResultSets.get(0).size()).isNotEqualTo(twoResultSets.get(1).size());
    }

    //this test doesn't pass for H2, checked for mySQL and works
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