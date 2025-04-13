package org.example.spring;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.CollectionPathBase;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mapping.PropertyPath;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

record PropertyPathInformationCustom (PropertyPath path) implements PathInformationCustom {
    PropertyPathInformationCustom(PropertyPath path) {
        this.path = path;
    }

    public static PropertyPathInformationCustom of(String path, Class<?> type) {
        return of(PropertyPath.from(path, type));
    }

    public static PropertyPathInformationCustom of(String path, TypeInformation<?> type) {
        return of(PropertyPath.from(path, type));
    }


    private static PropertyPathInformationCustom of(PropertyPath path) {
        return new PropertyPathInformationCustom(path);
    }

    public Class<?> getRootParentType() {
        return this.path.getOwningType().getType();
    }

    public Class<?> getLeafType() {
        return this.path.getLeafProperty().getType();
    }

    public Class<?> getLeafParentType() {
        return this.path.getLeafProperty().getOwningType().getType();
    }

    public String getLeafProperty() {
        return this.path.getLeafProperty().getSegment();
    }

    public TypeInformation<?> getPathType() {
        return this.path.getTypeInformation();
    }

    @Nullable
    public PropertyDescriptor getLeafPropertyDescriptor() {
        return BeanUtils.getPropertyDescriptor(this.getLeafParentType(), this.getLeafProperty());
    }

    public String toDotPath() {
        return this.path.toDotPath();
    }

    public Path<?> reifyPath(EntityPathResolver resolver) {
        return reifyPath(resolver, this.path, (Path)null);
    }

    private static Path<?> reifyPath(EntityPathResolver resolver, PropertyPath path, @Nullable Path<?> base) {
        if (base instanceof CollectionPathBase) {
            return reifyPath(resolver, path, (Path)((CollectionPathBase)base).any());
        } else {
            Path<?> entityPath = (Path<?>)(base != null ? base : resolver.createPath(path.getOwningType().getType()));
            Field field = ReflectionUtils.findField(entityPath.getClass(), path.getSegment());
            Object value = ReflectionUtils.getField(field, entityPath);
            return path.hasNext() ? reifyPath(resolver, path.next(), (Path)value) : (Path)value;
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof PathInformationCustom)) {
            return false;
        } else {
            PathInformationCustom that = (PathInformationCustom)o;
            return ObjectUtils.nullSafeEquals(this.getRootParentType(), that.getRootParentType()) && ObjectUtils.nullSafeEquals(this.toDotPath(), that.toDotPath());
        }
    }

    public int hashCode() {
        int result = ObjectUtils.nullSafeHashCode(this.getRootParentType());
        result = 31 * result + ObjectUtils.nullSafeHashCode(this.toDotPath());
        return result;
    }

    public String toString() {
        return "PropertyPathInformation(path=" + this.path + ")";
    }

    public PropertyPath path() {
        return this.path;
    }
}
