package querqy.converter;

import querqy.QueryConfig;
import querqy.model.ExpandedQuery;

import java.util.List;

public class TermListConverterFactory implements ConverterFactory<List<String>> {

    @Override
    public Converter<List<String>> createConverter(final ExpandedQuery expandedQuery, final QueryConfig queryConfig) {
        return TermListConverter.of(expandedQuery);
    }
}
