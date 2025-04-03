package org.example.controller;

import com.querydsl.core.types.Predicate;
import graphql.schema.DataFetchingEnvironment;
import org.example.entity.ParentEntity;
import org.example.repository.ParentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.querydsl.binding.QuerydslPredicateBuilder;
import org.springframework.data.util.TypeInformation;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller

public class ParentGraphQLController {

    private final QuerydslPredicateBuilder predicateBuilder;
    private final ParentRepository parentRepository;
    private final QuerydslBindingsFactory bindingsFactory;
    //private final PersistentEntities persistentEntities;


@Autowired
    public ParentGraphQLController(QuerydslPredicateBuilder predicateBuilder, ParentRepository parentRepository, QuerydslBindingsFactory bindingsFactory) {
        this.predicateBuilder = predicateBuilder;
        this.parentRepository = parentRepository;

    this.bindingsFactory = bindingsFactory;
   // this.persistentEntities = persistentEntities;
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
        arguments.forEach((key,value)->{

            if(value instanceof Iterable<?> iterable && key.equals("criteria")) {
                for(Object item : iterable) {
                    LinkedHashMap linkedHashMap = (LinkedHashMap) item;
                    multval.add((String) linkedHashMap.get("key"),(String)linkedHashMap.get("value"));
                }
            }
        });



        Predicate predicate = predicateBuilder.getPredicate(TypeInformation.of(ParentEntity.class),multval,bindingsFactory.createBindingsFor(TypeInformation.of(ParentEntity.class)));
        return (List<ParentEntity>) parentRepository.findAll(predicate);
    }




}
