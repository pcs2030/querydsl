package org.example.spring;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import org.example.common.code.Operator;
import org.springframework.data.querydsl.binding.MultiValueBinding;

import java.util.Collection;
import java.util.Optional;

@FunctionalInterface
interface MultiValueBindingCutom <T extends Path<? extends S>, S,P>  {
    Optional<Predicate> bind(T path, Collection<? extends S> value, P operator);
}
