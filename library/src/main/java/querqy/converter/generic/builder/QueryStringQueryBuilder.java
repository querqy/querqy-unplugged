package querqy.converter.generic.builder;

public interface QueryStringQueryBuilder<T> {

    T build(final String queryString);
}
