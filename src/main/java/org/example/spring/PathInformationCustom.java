package org.example.spring;

import com.querydsl.core.types.Path;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;

import java.beans.PropertyDescriptor;

public interface PathInformationCustom {
    Class<?> getRootParentType();

    Class<?> getLeafType();

    Class<?> getLeafParentType();

    String getLeafProperty();

    @Nullable
    PropertyDescriptor getLeafPropertyDescriptor();

    String toDotPath();

    Path<?> reifyPath(EntityPathResolver resolver);
    TypeInformation<?> getPathType();
}
