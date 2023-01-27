package querqy.converter.generic.builder;

import querqy.converter.generic.model.ExpandedQueryDefinition;

public interface ExpandedQueryBuilder<T> {

    T build(final ExpandedQueryDefinition<T> expandedQueryDefinition);
}
