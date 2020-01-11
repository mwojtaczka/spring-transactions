package com.maciej.springtransactions.service.propagation;

import com.maciej.springtransactions.model.Person;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class MyTxTemplateService {

    private final ServiceA serviceA;
    private final ServiceB serviceB;
    private final TransactionTemplate transactionTemplate;

    public MyTxTemplateService(ServiceA serviceA, ServiceB serviceB, TransactionTemplate transactionTemplate) {
        this.serviceA = serviceA;
        this.serviceB = serviceB;
        this.transactionTemplate = transactionTemplate;
    }

    public void methodWithTransactionTemplate() {

        transactionTemplate.execute(transactionStatus -> {
            Person personA = Person.builder()
                    .name("A").build();
            Person personB = Person.builder()
                    .name("B").build();

            serviceA.add(personA);
            serviceB.add(personB);

            return null;
        });
    }

    public void methodWithTxTemplateWhereOneServiceFailsWithRuntime() {

        transactionTemplate.executeWithoutResult(transactionStatus -> {
            Person personA = Person.builder()
                    .name("A").build();
            Person personB = Person.builder()
                    .name("B").build();

            serviceA.add(personA);
            serviceB.failWithRuntime();
        });
    }

}
