package org.example.spring;

import com.querydsl.core.types.Path;
import org.springframework.beans.BeanUtils;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.QuerydslUtils;


import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.beans.PropertyDescriptor;

public class QuerydslPathInformationCustom implements PathInformationCustom {
    private final Path<?> path;

    private QuerydslPathInformationCustom(Path<?> path) {
        this.path = path;
    }

    public static QuerydslPathInformationCustom of(Path<?> path) {
        return new QuerydslPathInformationCustom(path);
    }

    public Class<?> getRootParentType() {
        return this.path.getRoot().getType();
    }

    public Class<?> getLeafType() {
        return this.path.getType();
    }

    public Class<?> getLeafParentType() {
        Path<?> parent = this.path.getMetadata().getParent();
        if (parent == null) {
            throw new IllegalStateException(String.format("Could not obtain metadata for parent node of %s", this.path));
        } else {
            return parent.getType();
        }
    }

    public String getLeafProperty() {
        return this.path.getMetadata().getElement().toString();
    }

    @Nullable
    public PropertyDescriptor getLeafPropertyDescriptor() {
        return BeanUtils.getPropertyDescriptor(this.getLeafParentType(), this.getLeafProperty());
    }

    public String toDotPath() {
        return QuerydslUtils.toDotPath(this.path);
    }

    public Path<?> reifyPath(EntityPathResolver resolver) {
        return this.path;
    }

    @Override
    public TypeInformation<?> getPathType() {
        return null;
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
        return "QuerydslPathInformation(path=" + this.path + ")";
    }


    public static Object of(Object o) {
        return new QuerydslPathInformationCustom((Path<?>)o);
    }

    public Path<?> getPath() {
        return path;
    }
}