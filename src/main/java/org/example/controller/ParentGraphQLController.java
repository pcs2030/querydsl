package org.example.controller;

import com.querydsl.core.types.*;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import graphql.schema.DataFetchingEnvironment;
import org.example.common.code.DynamicQueryBuilder;
import org.example.common.code.FilterCriteria;
import org.example.common.code.FilterGroup;
import org.example.common.code.Operator;
import org.example.entity.ParentEntity;
import org.example.entity.*;
import org.example.repository.ParentRepository;
import org.example.spring.QuerydslBindingsCustom;
import org.example.spring.QuerydslPredicateBuilderCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.querydsl.binding.QuerydslPredicateBuilder;
import org.springframework.data.util.TypeInformation;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Field;
import java.util.*;

@Controller

public class ParentGraphQLController {

    private final QuerydslPredicateBuilder predicateBuilder;
    private final DynamicQueryBuilder dynamicQueryBuilder;
    private final ParentRepository parentRepository;
    private final QuerydslBindingsFactory bindingsFactory;
    private final QuerydslPredicateBuilderCustom predicateBuilderCustom;
    //private final PersistentEntities persistentEntities;
    private final JPAQueryFactory jpaQueryFactory;


    @Autowired
    public ParentGraphQLController(QuerydslPredicateBuilder predicateBuilder, DynamicQueryBuilder dynamicQueryBuilder, ParentRepository parentRepository, QuerydslBindingsFactory bindingsFactory, QuerydslPredicateBuilderCustom predicateBuilderCustom, JPAQueryFactory jpaQueryFactory) {
        this.predicateBuilder = predicateBuilder;
    this.dynamicQueryBuilder = dynamicQueryBuilder;
    this.parentRepository = parentRepository;

    this.bindingsFactory = bindingsFactory;
   // this.persistentEntities = persistentEntities;
    this.predicateBuilderCustom = predicateBuilderCustom;
    this.jpaQueryFactory = jpaQueryFactory;
}

    // Query to fetch all entities
//    @QueryMapping
//    public List<ParentEntity> parents(Map<String, String> filters) {
//        TypeInformation<ParentEntity> typeInformation = TypeInformation.of(ParentEntity.class);
//        QuerydslBindings bindings = new QuerydslBindings();
//
//        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
//        filters.forEach(parameters::add);
//
//        // Build the predicate
//        Predicate predicate = predicateBuilder.getPredicate(typeInformation, parameters, bindings);
//
//
//
//        return (List<ParentEntity>) parentRepository.findAll(predicate);
//    }


    @QueryMapping
    public List<ParentEntity> parents(DataFetchingEnvironment dataFetchingEnvironment) {

            Map<String, Object> arguments = dataFetchingEnvironment.getArguments();
            MultiValueMap<String, String> multval = new LinkedMultiValueMap<>();
            arguments.forEach((key,value)->{
                if(value instanceof Iterable<?> iterable) {
                    for(Object item : iterable) {
                        multval.add(key,item.toString());
                    }
                } else {
                    multval.add(key,value.toString());
                }
            });

            if(dataFetchingEnvironment.getArgument("childEntityName") != null) {
                multval.add("childEntities.name",dataFetchingEnvironment.getArgument("childEntityName"));
            }

        Predicate predicate = predicateBuilder.getPredicate(TypeInformation.of(ParentEntity.class),multval,bindingsFactory.createBindingsFor(TypeInformation.of(ParentEntity.class)));
        return (List<ParentEntity>) parentRepository.findAll(predicate);
    }


    @QueryMapping
    public List<ParentEntity> getData(DataFetchingEnvironment dataFetchingEnvironment) {

        Map<String, Object> arguments = dataFetchingEnvironment.getArguments();
        MultiValueMap<String, String> multval = new LinkedMultiValueMap<>();
        FilterGroup filterGroup = new FilterGroup();
        arguments.forEach((key,value)->{

            if(value instanceof Iterable<?> iterable && key.equals("criteria")) {
                for(Object item : iterable) {

                    LinkedHashMap linkedHashMap = (LinkedHashMap) item;
                    FilterCriteria filterCriteria = new FilterCriteria((String) linkedHashMap.get("key"), Collections.singletonList(linkedHashMap.get("value")), Operator.LIKE);
                    filterGroup.getAnd().add(filterCriteria);
                    multval.add((String) linkedHashMap.get("key"),(String)linkedHashMap.get("value"));
                }
            }
        });



        Predicate predicate = predicateBuilderCustom.getPredicate(TypeInformation.of(ParentEntity.class),filterGroup,new QuerydslBindingsCustom());
        List s=new ArrayList<>();
        s.add("name");
        s.add("childEntities.name");
        predicateBuilderCustom.prepareAll(TypeInformation.of(ParentEntity.class),filterGroup,new QuerydslBindingsCustom(),Collections.unmodifiableList(s));

        Predicate predicate1 = predicateBuilder.getPredicate(TypeInformation.of(ParentEntity.class),multval,bindingsFactory.createBindingsFor(TypeInformation.of(ParentEntity.class)));
        //return (List<ParentEntity>) parentRepository.findAll(predicate);

        QuerydslPredicateBuilderCustom.PredicateJoinsAndSelect predicateJoinsAndSelect=predicateBuilderCustom.prepareAll(TypeInformation.of(ParentEntity.class),filterGroup,new QuerydslBindingsCustom(),s);


        JPAQuery JQ =jpaQueryFactory.from(getEntityPath(ParentEntity.class));
        JQ.where( predicateJoinsAndSelect.predicates.toArray(Predicate[]::new));
        JQ.select(Projections.fields(ParentEntity.class,(Map<String, ? extends Expression<?>>) predicateJoinsAndSelect.expressionsMap));



            for (QuerydslPredicateBuilderCustom.JoinPath joins : predicateJoinsAndSelect.joins) {
//                JQ.leftJoin((CollectionExpression) joins.child());
            }


              //return   JQ.fetch();


//        JQ = jpaQueryFactory
//                .select(QParentEntity.parentEntity.name)
//                .from(QParentEntity.parentEntity)
//                //.where(emailPath.eq("someone@example.com"))
//                .leftJoin(QParentEntity.parentEntity.childEntities,QChildEntity.childEntity)
//                ;//.fetch();

        return   JQ.fetch();
    }


    public static EntityPath<?> getEntityPath(Class<?> entityClass) {
        try {
            String qClassName = entityClass.getPackageName() + ".Q" + entityClass.getSimpleName();
            Class<?> qClass = Class.forName(qClassName);

            // Most QueryDSL Q-classes have a static field matching the decapitalized name
            String fieldName = decapitalize(entityClass.getSimpleName());

            Field field = qClass.getField(fieldName);
            return (EntityPath<?>) field.get(null);

        } catch (Exception e) {
            throw new RuntimeException("Unable to resolve Q-class for: " + entityClass.getSimpleName(), e);
        }
    }

    private static String decapitalize(String name) {
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }




}
