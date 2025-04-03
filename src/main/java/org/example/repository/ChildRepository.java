package org.example.repository;

import org.example.entity.ChildEntity;
import org.example.entity.ParentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface ChildRepository extends JpaRepository<ChildEntity, Long>, QuerydslPredicateExecutor<ChildEntity> {
}

