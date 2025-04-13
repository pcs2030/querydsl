package org.example.common.code;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.util.TypeInformation;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DynamicQueryBuilder {
    private final ConversionService conversionService;
    private final EntityPathResolver entityPathResolver = SimpleEntityPathResolver.INSTANCE;

    public DynamicQueryBuilder(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    // Build predicate with filters
    public <T> Predicate buildPredicate(TypeInformation<T> entityClass, FilterGroup group) {
        PathBuilder<?> root = new PathBuilder<>(entityClass.getType(),"parentEntity");
        BooleanBuilder finalPredicate = new BooleanBuilder();

        // AND filters
        if (group.getAnd() != null) {
            for (FilterCriteria filter : group.getAnd()) {
                finalPredicate.and(buildSinglePredicate(root, entityClass.getType(), filter));
            }
        }

        // OR filters
        if (group.getOr() != null && !group.getOr().isEmpty()) {
            BooleanBuilder orBuilder = new BooleanBuilder();
            for (FilterCriteria filter : group.getOr()) {
                orBuilder.or(buildSinglePredicate(root, entityClass.getType(), filter));
            }
            finalPredicate.and(orBuilder);
        }

        return finalPredicate;
    }

    // Build select expressions based on field names
    public <T> List<Expression<?>> buildSelectExpressions(Class<T> entityClass, List<String> fieldPaths) {
        PathBuilder<T> root = new PathBuilder<>(entityClass, entityClass.getSimpleName().toLowerCase());
        List<Expression<?>> expressions = new ArrayList<>();

        for (String fieldPath : fieldPaths) {
            expressions.add(resolvePath(root, entityClass, fieldPath));
        }

        return expressions;
    }

    // Build single predicate (comparison like EQ, GT, etc.)
    private <T> Predicate buildSinglePredicate(PathBuilder<T> root, Class<?> type, FilterCriteria filter) {
        Expression<?> path = resolvePath(root, type, filter.getField());
        Object value = conversionService.convert(filter.getValue(), getFieldType(type, filter.getField()));

        return switch (filter.getOperator()) {
            case EQ -> Expressions.predicate(Ops.EQ, path, Expressions.constant(value));
            case NE -> Expressions.predicate(Ops.NE, path, Expressions.constant(value));
            case GT -> Expressions.predicate(Ops.GT, path, Expressions.constant(value));
            case GTE -> Expressions.predicate(Ops.GOE, path, Expressions.constant(value));
            case LT -> Expressions.predicate(Ops.LT, path, Expressions.constant(value));
            case LTE -> Expressions.predicate(Ops.LOE, path, Expressions.constant(value));
            case LIKE -> Expressions.predicate(Ops.LIKE, path, Expressions.constant("%" + value + "%"));
        };
    }

    // Resolve path for field
    private Expression<?> resolvePath(PathBuilder<?> root, Class<?> clazz, String fieldPath) {
        String[] parts = fieldPath.split("\\.");
        PathBuilder<?> current = root;
        Class<?> currentType = clazz;

        for (int i = 0; i < parts.length - 1; i++) {
            currentType = getFieldType(currentType, parts[i]);
            current = current.get(parts[i], currentType);
        }

        String last = parts[parts.length - 1];
        Class<?> lastType = getFieldType(currentType, last);
        // If the last part is a collection (List, Set), we perform a JOIN
        if (List.class.isAssignableFrom(lastType)) {
            // For collections, we'll join and fetch the child collection
            return current.get(last).as(String.valueOf(lastType));  // Handle the collection as a join
        }
        return current.get(last, lastType);
    }

    // Get field type dynamically
    private Class<?> getFieldType(Class<?> clazz, String fieldPath) {
        String[] parts = fieldPath.split("\\.");
        Class<?> currentClass = clazz;

        for (String part : parts) {
            boolean found = false;

            while (currentClass != null && !found) {
                for (Field field : currentClass.getDeclaredFields()) {
                    if (field.getName().equals(part)) {
                        found = true;
                        Class<?> rawType = field.getType();

                        // Handle generics (e.g., List<Child>)
                        if (Collection.class.isAssignableFrom(rawType)) {
                            Type genericType = field.getGenericType();
                            if (genericType instanceof ParameterizedType) {
                                Type actualType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
                                if (actualType instanceof Class<?>) {
                                    currentClass = (Class<?>) actualType;
                                } else {
                                    // Fallback to raw List if no actual type info
                                    currentClass = Object.class; // or Collection.class if you prefer
                                }
                            } else {
                                currentClass = Object.class; // Or rawType if you want to preserve collection
                            }
                        }  else {
                            currentClass = rawType;
                            break;
                        }
                    }
                }

                // Move up to superclass if not found
                if (!found) {
                    currentClass = currentClass.getSuperclass();
                }
            }

            if (!found || currentClass == null) {
                throw new RuntimeException("Can't resolve field: " + part + " in path: " + fieldPath);
            }
        }

        return currentClass;
    }

    // Apply join logic based on path field (handles nested or collection types)
    public <T> JPAQuery<?> applyJoinLogic(JPAQuery<?> query, Class<T> entityClass, PathBuilder<T> root, List<String> fieldPaths) {
        for (String fieldPath : fieldPaths) {
            String[] parts = fieldPath.split("\\.");
            if (parts.length > 1) {
                String joinField = parts[0];
                query.leftJoin(root.get(joinField));
            }
        }
        return query;
    }
}
