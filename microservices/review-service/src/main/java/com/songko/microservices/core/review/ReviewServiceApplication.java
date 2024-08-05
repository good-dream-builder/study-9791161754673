package com.songko.microservices.core.review;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executors;

@Slf4j
@SpringBootApplication
@ComponentScan("com.songko")
public class ReviewServiceApplication {
    private final Integer connectionPoolSize;

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(ReviewServiceApplication.class, args);

        String mysqlUri = ctx.getEnvironment().getProperty("spring.datasource.url");
        log.info("Connected to MySQL: " + mysqlUri);
    }
    
    @Autowired
    public ReviewServiceApplication(
            @Value("${spring.datasource.maximum-pool-size:10}")
                    Integer connectionPoolSize
    ) {
        this.connectionPoolSize = connectionPoolSize;
    }

    @Bean
    public Scheduler jdbcScheduler() {
        log.info("Creates a jdbcScheduler with connectionPoolSize = " + connectionPoolSize);
        return Schedulers.fromExecutor(Executors.newFixedThreadPool(connectionPoolSize));
    }


}
