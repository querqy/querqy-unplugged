package querqy.converter.generic.builder;

import java.util.List;

public interface PhraseQueryBuilder<T> {

    T build(String field, List<String> terms, int slop, float boost);
}
