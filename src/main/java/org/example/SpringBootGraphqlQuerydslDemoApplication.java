package org.example;

import org.example.configuration.ServicesConfigurations;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EntityScan(basePackages = "org.example.entity")
@Import(ServicesConfigurations.class)
public class SpringBootGraphqlQuerydslDemoApplication
{
  public static void main(String[] args) {
    // start the application
    SpringApplication.run(SpringBootGraphqlQuerydslDemoApplication.class, args);
  }
}
