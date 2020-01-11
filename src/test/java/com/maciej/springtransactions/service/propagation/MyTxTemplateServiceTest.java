package com.maciej.springtransactions.service.propagation;

import com.maciej.springtransactions.repository.InMemoryRepo;
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
class MyTxTemplateServiceTest {

    @Autowired
    private MyTxTemplateService myTxTemplateService;

    @Autowired
    private InMemoryRepo repo;

    @BeforeEach
    void cleanDb() {
        repo.deleteAll();
    }

    @Test
    void shouldCommitTransaction() {

        myTxTemplateService.methodWithTransactionTemplate();

        assertThat(repo.findAll()).hasSize(2);
    }

    @Test
    void shouldRollbackTransaction_whenOneServiceFailsWithRuntime() {

        assertThrows(RuntimeException.class, () -> myTxTemplateService.methodWithTxTemplateWhereOneServiceFailsWithRuntime());

        assertThat(repo.findAll()).isEmpty();
    }

}