package org.example.spring;

import com.querydsl.core.types.Path;
import org.example.common.code.Operator;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.querydsl.binding.*;
import org.springframework.data.util.Optionals;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;

public class QuerydslBindingsCustom {
    private final Map<String, QuerydslBindingsCustom.PathAndBindingCustom<?, ?>> pathSpecs = new LinkedHashMap();
    private final Map<Class<?>, QuerydslBindingsCustom.PathAndBindingCustom<?, ?>> typeSpecs = new LinkedHashMap();
    private final Set<String> allowList = new HashSet();
    private final Set<String> denyList = new HashSet();
    private final Set<String> aliases = new HashSet();
    private boolean excludeUnlistedProperties;

    public QuerydslBindingsCustom() {
    }

    public final <T extends Path<S>, S> QuerydslBindingsCustom.AliasingPathBinder<T, S> bind(T path) {
        return new QuerydslBindingsCustom.AliasingPathBinder<T, S>(path);
    }

    @SafeVarargs
    public final <T extends Path<S>, S> QuerydslBindingsCustom.PathBinder<T, S> bind(T... paths) {
        return new QuerydslBindingsCustom.PathBinder<T, S>(paths);
    }

    public final <T> QuerydslBindingsCustom.TypeBinder<T> bind(Class<T> type) {
        return new QuerydslBindingsCustom.TypeBinder<T>(type);
    }

    public final void excluding(Path<?>... paths) {
        Assert.notEmpty(paths, "At least one path has to be provided");

        for(Path<?> path : paths) {
            this.denyList.add(toDotPath(Optional.of(path)));
        }

    }

    public final void including(Path<?>... paths) {
        Assert.notEmpty(paths, "At least one path has to be provided");

        for(Path<?> path : paths) {
            this.allowList.add(toDotPath(Optional.of(path)));
        }

    }

    public final QuerydslBindingsCustom excludeUnlistedProperties(boolean excludeUnlistedProperties) {
        this.excludeUnlistedProperties = excludeUnlistedProperties;
        return this;
    }

    boolean isPathAvailable(String path, Class<?> type) {
        Assert.notNull(path, "Path must not be null");
        Assert.notNull(type, "Type must not be null");
        return this.isPathAvailable(path, TypeInformation.of(type));
    }

    boolean isPathAvailable(String path, TypeInformation<?> type) {
        Assert.notNull(path, "Path must not be null");
        Assert.notNull(type, "Type must not be null");
        return this.getPropertyPath(path, type) != null;
    }

    public <S extends Path<? extends T>, T> Optional<MultiValueBindingCutom<S, T,Operator>> getBindingForPath(PathInformationCustom path) {
        Assert.notNull(path, "PropertyPath must not be null");
        QuerydslBindingsCustom.PathAndBindingCustom<S, T> PathAndBindingCustom = (QuerydslBindingsCustom.PathAndBindingCustom)this.pathSpecs.get(createKey(path));
        if (PathAndBindingCustom != null) {
            Optional<MultiValueBindingCutom<S, T,Operator>> binding = PathAndBindingCustom.getBinding();
            if (binding.isPresent()) {
                return binding;
            }
        }

        PathAndBindingCustom = (QuerydslBindingsCustom.PathAndBindingCustom)this.typeSpecs.get(path.getLeafType());
        return PathAndBindingCustom == null ? Optional.empty() : PathAndBindingCustom.getBinding();
    }

    Optional<Path<?>> getExistingPath(PathInformationCustom path) {
        Assert.notNull(path, "PropertyPath must not be null");
        return Optional.ofNullable((QuerydslBindingsCustom.PathAndBindingCustom)this.pathSpecs.get(createKey(path))).flatMap(QuerydslBindingsCustom.PathAndBindingCustom::getPath);
    }

    @Nullable
    PathInformationCustom getPropertyPath(String path, TypeInformation<?> type) {
        Assert.notNull(path, "Path must not be null");
        Assert.notNull(type, "Type information must not be null");
        if (!this.isPathVisible(path)) {
            return null;
        } else {
            String key = createKey(type, path);

            if (this.pathSpecs.containsKey(key)) {
                return (PathInformationCustom) ((QuerydslBindingsCustom.PathAndBindingCustom)this.pathSpecs.get(key)).getPath().map(QuerydslPathInformationCustom::of).orElse((Object)null);
            } else if (this.pathSpecs.containsKey(path)) {
                return (PathInformationCustom)((QuerydslBindingsCustom.PathAndBindingCustom)this.pathSpecs.get(path)).getPath().map(QuerydslPathInformationCustom::of).orElse((Object)null);
            } else {
                try {
                    PathInformationCustom propertyPath = PropertyPathInformationCustom.of(path, type);
                    return this.isPathVisible(propertyPath) ? propertyPath : null;
                } catch (PropertyReferenceException var5) {
                    return null;
                }
            }
        }
    }

    private static String createKey(Optional<Path<?>> path) {
        return (String)path.map(QuerydslPathInformationCustom::of).map(QuerydslBindingsCustom::createKey).orElse("");
    }

    private static String createKey(PathInformationCustom path) {
        return createKey(path.getRootParentType(), path.toDotPath());
    }

    private static String createKey(TypeInformation<?> type, String path) {
        return createKey(type.getType(), path);
    }

    private static String createKey(Class<?> type, String path) {
        String var10000 = type.getSimpleName();
        return var10000 + "." + path;
    }

    private boolean isPathVisible(PathInformationCustom path) {
        List<String> segments = Arrays.asList(path.toDotPath().split("\\."));

        for(int i = 1; i <= segments.size(); ++i) {
            if (!this.isPathVisible(StringUtils.collectionToDelimitedString(segments.subList(0, i), "."))) {
                if (!this.allowList.isEmpty()) {
                    return this.allowList.contains(path.toDotPath());
                }

                return false;
            }
        }

        return true;
    }

    private boolean isPathVisible(String path) {
        if (this.aliases.contains(path) && !this.denyList.contains(path)) {
            return true;
        } else if (this.allowList.isEmpty()) {
            return this.excludeUnlistedProperties ? false : !this.denyList.contains(path);
        } else {
            return this.allowList.contains(path);
        }
    }

    private static String toDotPath(Optional<Path<?>> path) {
        return (String)path.map(QuerydslBindingsCustom::fromRootPath).orElse("");
    }

    private static String fromRootPath(Path<?> path) {
        Path<?> rootPath = path.getMetadata().getRootPath();
        if (rootPath == null) {
            throw new IllegalStateException(String.format("Couldn't find root path on path %s", path));
        } else {
            return path.toString().substring(rootPath.getMetadata().getName().length() + 1);
        }
    }

    public class PathBinder<P extends Path<? extends T>, T> {
        private final List<P> paths;

        @SafeVarargs
        PathBinder(P... paths) {
            Assert.notEmpty(paths, "At least one path has to be provided");
            this.paths = Arrays.asList(paths);
        }

        public void firstOptional(OptionalValueBinding<P, T> binding) {
            Assert.notNull(binding, "Binding must not be null");
            this.all((path, value,operator) -> binding.bind(path, Optionals.next(value.iterator())));
        }

        public void first(SingleValueBinding<P, T> binding) {
            Assert.notNull(binding, "Binding must not be null");
            this.all((path, value,operator) -> Optionals.next(value.iterator()).map((t) -> binding.bind(path, t)));
        }

        public void all(MultiValueBindingCutom<P, T,Operator> binding) {
            Assert.notNull(binding, "Binding must not be null");
            this.paths.forEach((path) -> this.registerBinding(QuerydslBindingsCustom.PathAndBindingCustom.withPath(path).with(binding)));
        }

        protected void registerBinding(QuerydslBindingsCustom.PathAndBindingCustom<P, T> binding) {
            QuerydslBindingsCustom.this.pathSpecs.put(QuerydslBindingsCustom.createKey(binding.getPath()), binding);
        }
    }

    public class AliasingPathBinder<P extends Path<? extends T>, T> extends QuerydslBindingsCustom.PathBinder<P, T> {
        @Nullable
        private final String alias;
        private final P path;

        AliasingPathBinder(P path) {
            this((String)null, path);
        }

        private AliasingPathBinder(@Nullable String alias, P path) {
            super(path);
            Assert.notNull(path, "Path must not be null");
            this.alias = alias;
            this.path = path;
        }

        public QuerydslBindingsCustom.AliasingPathBinder<P, T> as(String alias) {
            Assert.hasText(alias, "Alias must not be null or empty");
            return QuerydslBindingsCustom.this.new AliasingPathBinder<P, T>(alias, this.path);
        }

        public void withDefaultBinding() {
            this.registerBinding(QuerydslBindingsCustom.PathAndBindingCustom.withPath(this.path));
        }

        protected void registerBinding(QuerydslBindingsCustom.PathAndBindingCustom<P, T> binding) {
            super.registerBinding(binding);
            String dotPath = QuerydslBindingsCustom.toDotPath(binding.getPath());
            if (this.alias != null) {
                QuerydslBindingsCustom.this.pathSpecs.put(this.alias, binding);
                QuerydslBindingsCustom.this.aliases.add(this.alias);
                QuerydslBindingsCustom.this.denyList.add(dotPath);
            }

        }
    }

    public final class TypeBinder<T> {
        private final Class<T> type;

        public TypeBinder(Class<T> type) {
            this.type = type;
        }

        public <P extends Path<T>> void firstOptional(OptionalValueBinding<P, T> binding) {
            Assert.notNull(binding, "Binding must not be null");
            this.all((path, value,operator) -> binding.bind((P) path, Optionals.next(value.iterator())));
        }

        public <P extends Path<T>> void first(SingleValueBinding<P, T> binding) {
            Assert.notNull(binding, "Binding must not be null");
            this.all((path, value,operator) -> Optionals.next(value.iterator()).map((t) -> binding.bind((P) path, t)));
        }

        public <P extends Path<T>> void all(MultiValueBindingCutom<P, T,Operator> binding) {
            Assert.notNull(binding, "Binding must not be null");
            QuerydslBindingsCustom.this.typeSpecs.put(this.type, QuerydslBindingsCustom.PathAndBindingCustom.withoutPath().with((MultiValueBindingCutom<Path<?>, Object,Operator>) binding));
        }
    }

    private static final class PathAndBindingCustom<P extends Path<? extends T>, T> {
        private final Optional<Path<?>> path;
        private final Optional<MultiValueBindingCutom<P, T, Operator>> binding;

        PathAndBindingCustom(Optional<Path<?>> path, Optional<MultiValueBindingCutom<P, T,Operator>> binding) {
            this.path = path;
            this.binding = binding;
        }

        public static <T, P extends Path<? extends T>> QuerydslBindingsCustom.PathAndBindingCustom<P, T> withPath(P path) {
            return new QuerydslBindingsCustom.PathAndBindingCustom<P, T>(Optional.of(path), Optional.empty());
        }

        public static <T, S extends Path<? extends T>> QuerydslBindingsCustom.PathAndBindingCustom<S, T> withoutPath() {
            return new QuerydslBindingsCustom.PathAndBindingCustom<S, T>(Optional.empty(), Optional.empty());
        }

        public QuerydslBindingsCustom.PathAndBindingCustom<P, T> with(MultiValueBindingCutom<P, T,Operator> binding) {
            return new QuerydslBindingsCustom.PathAndBindingCustom<P, T>(this.path, Optional.of(binding));
        }

        public Optional<Path<?>> getPath() {
            return this.path;
        }

        public Optional<MultiValueBindingCutom<P, T,Operator>> getBinding() {
            return this.binding;
        }

        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            } else if (o instanceof QuerydslBindingsCustom.PathAndBindingCustom) {
                QuerydslBindingsCustom.PathAndBindingCustom<?, ?> that = (QuerydslBindingsCustom.PathAndBindingCustom)o;
                return !ObjectUtils.nullSafeEquals(this.path, that.path) ? false : ObjectUtils.nullSafeEquals(this.binding, that.binding);
            } else {
                return false;
            }
        }

        public int hashCode() {
            int result = ObjectUtils.nullSafeHashCode(this.path);
            result = 31 * result + ObjectUtils.nullSafeHashCode(this.binding);
            return result;
        }

        public String toString() {
            Optional var10000 = this.getPath();
            return "QuerydslBindingsCustom.PathAndBindingCustom(path=" + var10000 + ", binding=" + this.getBinding() + ")";
        }
    }
}
