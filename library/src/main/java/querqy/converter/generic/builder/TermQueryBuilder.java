package querqy.converter.generic.builder;

import querqy.converter.generic.model.TermQueryDefinition;

public interface TermQueryBuilder<T> {

    T build(final TermQueryDefinition termQueryDefinition);

}
