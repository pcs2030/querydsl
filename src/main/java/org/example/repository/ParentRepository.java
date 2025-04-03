package org.example.repository;

import org.example.entity.ParentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface ParentRepository extends JpaRepository<ParentEntity, Long>, QuerydslPredicateExecutor<ParentEntity> {
}

