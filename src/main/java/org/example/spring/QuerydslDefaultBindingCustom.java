package org.example.spring;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.*;
import org.example.common.code.Operator;
import org.springframework.data.querydsl.binding.MultiValueBinding;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Optional;

public class QuerydslDefaultBindingCustom implements MultiValueBindingCutom<Path<? extends Object>, Object,Operator> {
    QuerydslDefaultBindingCustom() {
    }

    public Optional<Predicate> bind(Path<?> path, Collection<? extends Object> value, Operator operator) {
        Assert.notNull(path, "Path must not be null");
        Assert.notNull(value, "Value must not be null");
        if (value.isEmpty()) {
            return Optional.empty();
        } else if (!(path instanceof CollectionPathBase)) {
            //if (path instanceof SimpleExpression) {
                SimpleExpression expression = (SimpleExpression) path;
                if (value.size() > 1) {
                    return Optional.of(expression.in(value));
                } else {
                    Object object = value.iterator().next();
                    return Optional.of(object == null ? expression.isNull() : getBooleanExpression(path,operator,object));
                }
            //} else {
                //throw new IllegalArgumentException(String.format("Cannot create predicate for path '%s' with type '%s'", path, path.getMetadata().getPathType()));
            //}
        } else {
            CollectionPathBase cpb = (CollectionPathBase) path;
            BooleanBuilder builder = new BooleanBuilder();

            for (Object element : value) {
                if (element instanceof Collection) {
                    for (Object nested : (Collection) element) {
                        builder.and(cpb.contains(nested));
                    }
                } else {
                    builder.and(cpb.contains(element));
                }
            }

            return Optional.of(builder.getValue());
        }
    }



    private BooleanExpression getBooleanExpression (Path<?> path, Operator operator, Object object) {

        switch (operator) {
            case EQ -> {
                BooleanExpression eq = Expressions.predicate(Ops.EQ, path, Expressions.constant(object));
                return eq;
            }
            case NE -> {
                BooleanExpression eq = Expressions.predicate(Ops.NE, path, Expressions.constant(object));
                return eq;
            }
            case LTE -> {
                BooleanExpression eq = Expressions.predicate(Ops.LOE, path, Expressions.constant(object));
                return eq;
            }
            case GTE -> {
                BooleanExpression eq = Expressions.predicate(Ops.GOE, path, Expressions.constant(object));
                return eq;
            }
            case LT -> {
                BooleanExpression eq = Expressions.predicate(Ops.LT, path, Expressions.constant(object));
                return eq;
            }
            case GT -> {
                BooleanExpression eq = Expressions.predicate(Ops.GT, path, Expressions.constant(object));
                return eq;
            }
            case LIKE -> {
                if (path instanceof StringPath sp) {
                    return sp.containsIgnoreCase((String) object);
                }
            }
            default -> throw new RuntimeException("Opration %s not supported "+operator);


        }
        return null;
    }

}
