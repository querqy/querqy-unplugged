package querqy.converter.generic.builder;

public interface ConstantScoreQueryBuilder<T> {

    T build(final T query, final float constantScore);

}
