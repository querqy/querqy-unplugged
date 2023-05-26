package querqy.converter.generic.builder;

public interface WrappedQueryBuilder<T> {

    T wrap(final T query);

    static <T> WrappedQueryBuilder<T> defaultBuilder() {
        return x -> x;
    }
}
