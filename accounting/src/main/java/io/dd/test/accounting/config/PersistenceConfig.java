package io.dd.test.accounting.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;

@Configuration
public class PersistenceConfig {

    @Primary
    @Bean
    public JpaTransactionManager transactionManager() { //don't change bean name. If it's not 'transactionManager' then it will fail on JpaRepository
        return new JpaTransactionManager();
    }

}
