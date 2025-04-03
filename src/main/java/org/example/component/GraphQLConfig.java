package org.example.component;

import org.example.repository.ParentRepository;
import org.springframework.stereotype.Component;

@Component
public class GraphQLConfig {

    private final ParentRepository parentRepository;

    public GraphQLConfig(ParentRepository parentRepository) {
        this.parentRepository = parentRepository;
    }







}

