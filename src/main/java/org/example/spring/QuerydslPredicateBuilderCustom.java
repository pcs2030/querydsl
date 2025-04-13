package org.example.spring;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathType;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.ListPath;
import org.example.common.code.FilterCriteria;
import org.example.common.code.FilterGroup;
import org.example.common.code.Operator;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.Property;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

import java.beans.PropertyDescriptor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QuerydslPredicateBuilderCustom {
    private final ConversionService conversionService;
    private final MultiValueBindingCutom<Path<? extends Object>, Object,Operator> defaultBinding;
    private final Map<PathInformationCustom, Path<?>> paths;
    private final EntityPathResolver resolver;

    public QuerydslPredicateBuilderCustom(ConversionService conversionService, EntityPathResolver resolver) {
        Assert.notNull(conversionService, "ConversionService must not be null");
        this.defaultBinding = (MultiValueBindingCutom<Path<? extends Object>, Object, Operator>) new QuerydslDefaultBindingCustom();
        this.conversionService = conversionService;
        this.paths = new ConcurrentHashMap();
        this.resolver = resolver;
    }

    public class PredicateJoinsAndSelect {

        public final List<Expression> expressions = new ArrayList<>();
        public final Map<String, Expression<?>> expressionsMap = new HashMap<>();
        public final HashSet<JoinPath> joins = new HashSet<>();
        public final List<Predicate> predicates = new ArrayList<>();

    }
    public record JoinPath(Path<?> parent, Path<?> child) {
        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            JoinPath joinPath = (JoinPath) o;
            return Objects.equals(child, joinPath.child) && Objects.equals(parent, joinPath.parent);
        }

        @Override
        public int hashCode() {
            return Objects.hash(parent, child);
        }
    }


    public PredicateJoinsAndSelect prepareAll(TypeInformation<?> type, FilterGroup values, QuerydslBindingsCustom bindings,List<String> selectColumns) {
        Assert.notNull(bindings, "Context must not be null");
        BooleanBuilder builder = new BooleanBuilder();

        PredicateJoinsAndSelect predicateJoinsAndSelect = new PredicateJoinsAndSelect();

        Set<String> allPaths = new HashSet<>();
        allPaths.addAll(selectColumns);
        allPaths.addAll(values.getAndKeys());
        allPaths.addAll(values.getOrKeys());
        Map<String,FilterCriteria> andFilters = values.getAndMap();
        Map<String,FilterCriteria> orFilters = values.getOrMap();

        if (allPaths.isEmpty()) {

            //TODO Add alljoins
            return predicateJoinsAndSelect;
        } else {

            for(String path : allPaths) {
                if (bindings.isPathAvailable(path, type)) {
                    PathInformationCustom propertyPath = bindings.getPropertyPath(path, type);
                    if (propertyPath != null) {
                        Path<?> path2 = this.getPath(propertyPath, bindings);
                        if(selectColumns.contains(path)){
                            predicateJoinsAndSelect.expressions.add((Expression) path2);
                            predicateJoinsAndSelect.expressionsMap.put(path,(Expression<?>) path2);
                        }

                        if(andFilters.containsKey(path)){
                            Collection<Object> value = this.convertToPropertyPathSpecificType(andFilters.get(path).getValue(), propertyPath);
                            Optional<Predicate> predicate = this.invokeBinding(propertyPath, bindings, value,andFilters.get(path).getOperator());
                            Objects.requireNonNull(builder);
                            predicate.ifPresent(builder::and);
                        } else if(orFilters.containsKey(path)) {
                            Collection<Object> value = this.convertToPropertyPathSpecificType(andFilters.get(path).getValue(), propertyPath);
                            Optional<Predicate> predicate = this.invokeBinding(propertyPath, bindings, value, andFilters.get(path).getOperator());
                            Objects.requireNonNull(builder);
                            predicate.ifPresent(builder::or);
                        }

                        if(path2.getMetadata().getParent().getMetadata().getPathType().equals(PathType.COLLECTION_ANY)) {
                            predicateJoinsAndSelect.joins.add(new JoinPath(path2.getRoot(),((ListPath)path2.getMetadata().getParent().getMetadata().getParent())));
                        }
                        predicateJoinsAndSelect.predicates.add(getPredicate(builder));
                    }

                }
            }

            return predicateJoinsAndSelect;
        }
    }

    public Predicate getPredicate(TypeInformation<?> type, FilterGroup values, QuerydslBindingsCustom bindings) {
        Assert.notNull(bindings, "Context must not be null");
        BooleanBuilder builder = new BooleanBuilder();
        if (values.getAnd().isEmpty()) {
            return getPredicate(builder);
        } else {
            for(FilterCriteria filterCriteria : values.getAnd()) {
                if (filterCriteria.getField()!= null) {
                    String path = (String)filterCriteria.getField();
                    if (bindings.isPathAvailable(path, type)) {
                        PathInformationCustom propertyPath = bindings.getPropertyPath(path, type);
                        if (propertyPath != null) {
                            Collection<Object> value = this.convertToPropertyPathSpecificType(filterCriteria.getValue(), propertyPath);
                            Optional<Predicate> predicate = this.invokeBinding(propertyPath, bindings, value,filterCriteria.getOperator());
                            Objects.requireNonNull(builder);
                            predicate.ifPresent(builder::and);
                        }
                    }
                }
            }

            return getPredicate(builder);
        }
    }

    public static boolean isEmpty(Predicate predicate) {
        return (new BooleanBuilder()).equals(predicate);
    }

    private Optional<Predicate> invokeBinding(PathInformationCustom dotPath, QuerydslBindingsCustom bindings, Collection<Object> values, Operator operator) {
        Path<?> path = this.getPath(dotPath, bindings);
        return ((MultiValueBindingCutom)bindings.getBindingForPath(dotPath).orElse((MultiValueBindingCutom<Path<?>, Object, Operator>) this.defaultBinding)).bind(path, values,operator);

    }

    private Path<?> getPath(PathInformationCustom path, QuerydslBindingsCustom bindings) {
        Optional<Path<?>> resolvedPath = bindings.getExistingPath(path);
        return (Path)resolvedPath.orElseGet(() -> (Path)this.paths.computeIfAbsent(path, (it) -> it.reifyPath(this.resolver)));
    }

    private Collection<Object> convertToPropertyPathSpecificType(List<?> source, PathInformationCustom path) {
        if (!source.isEmpty() && !isSingleElementCollectionWithEmptyItem(source)) {
            TypeDescriptor targetType = getTargetTypeDescriptor(path);
            Collection<Object> target = new ArrayList(source.size());

            for(Object value : source) {
                target.add(this.getValue(targetType, value));
            }

            return target;
        } else {
            return Collections.emptyList();
        }
    }

    @Nullable
    private Object getValue(TypeDescriptor targetType, Object value) {
        if (ClassUtils.isAssignableValue(targetType.getType(), value)) {
            return value;
        } else {
            return this.conversionService.canConvert(value.getClass(), targetType.getType()) ? this.conversionService.convert(value, TypeDescriptor.forObject(value), targetType) : value;
        }
    }

    private static TypeDescriptor getTargetTypeDescriptor(PathInformationCustom path) {
        PropertyDescriptor descriptor = path.getLeafPropertyDescriptor();
        Class<?> owningType = path.getLeafParentType();
        String leafProperty = path.getLeafProperty();
        TypeDescriptor result = descriptor == null ? TypeDescriptor.nested(ReflectionUtils.findRequiredField(owningType, leafProperty), 0) : TypeDescriptor.nested(new Property(owningType, descriptor.getReadMethod(), descriptor.getWriteMethod(), leafProperty), 0);
        if (result == null) {
            throw new IllegalStateException(String.format("Could not obtain TypeDescriptor for PathInformationCustom %s", path));
        } else {
            return result;
        }
    }

    private static boolean isSingleElementCollectionWithEmptyItem(List<?> source) {
        return source.size() == 1 && ObjectUtils.isEmpty(source.get(0));
    }

    private static Predicate getPredicate(BooleanBuilder builder) {
        Predicate predicate = builder.getValue();
        return (Predicate)(predicate == null ? new BooleanBuilder() : predicate);
    }
}
