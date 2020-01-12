#Spring transactions
This repo contains examples of Spring transactions usage.
##Where to start
To start I recommend clone repo and go through code starting from test classes:
- MyAnnotatedServiceTest - contains usage of @Transactional annotation
- MyAnnotatedServicePropagationTest - again @Transactional annotation but with couple of examples of 
propagation setting.
- MyTxTemplateServiceTest - @Transactional is not only way of creating transaction in Spring
very simple usage of TransactionTemplate
-IsolationTest - lets check how @Transactional meets isolation levels (it depends on particular DB vendor - tested with 
H2 and MySQL - if u want to change DB just go to application.yaml and put url and add connector to pom)  