package querqy.converter.generic.builder;

public interface RawQueryBuilder<T> {

    T buildFromString(String rawQueryString);

}
