package org.example.configuration;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.common.code.DynamicQueryBuilder;
import org.example.entity.ParentEntity;
import org.example.spring.QuerydslPredicateBuilderCustom;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.querydsl.binding.QuerydslPredicateBuilder;
import org.springframework.graphql.data.query.QuerydslDataFetcher;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

public class ServicesConfigurations {
//    @Bean
//    public GenericQuerydslService<ParentEntity> service(ParentRepository repository) {
//        return new GenericQuerydslService<>(repository, QParentEntity.parentEntity);
//    }

    @PersistenceContext
    private EntityManager entityManager;
//
//    @Bean
//    public ConversionService conversionService() {
//        return new DefaultConversionService();
//    }

    @Bean
    public QuerydslPredicateBuilder predicateBuilder(ConversionService conversionService, QuerydslBindingsFactory bindingsFactory) {
        return new QuerydslPredicateBuilder(conversionService,bindingsFactory.getEntityPathResolver());
    }

    @Bean
    public DynamicQueryBuilder dynamicQueryBuilder(ConversionService conversionService) {
        return new DynamicQueryBuilder(conversionService);
    }

    @Bean
    public QuerydslPredicateBuilderCustom predicateBuilderCustom(ConversionService conversionService, QuerydslBindingsFactory bindingsFactory) {
        return new QuerydslPredicateBuilderCustom(conversionService,bindingsFactory.getEntityPathResolver());
    }

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

//
//    @Bean
//    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
//        DataFetcher<Iterable<ParentEntity>> dataFetcher = QuerydslDataFetcher.builder(parentRepository).many();
//
//        return wiringBuilder -> wiringBuilder
//                .type("Query", typeWiring -> typeWiring.dataFetcher("parents", dataFetcher));
//    }

}
