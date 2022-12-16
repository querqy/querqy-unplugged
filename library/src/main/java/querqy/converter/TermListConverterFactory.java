package querqy.converter;

import lombok.NoArgsConstructor;
import querqy.model.ExpandedQuery;

import java.util.List;

@NoArgsConstructor(staticName = "create")
public class TermListConverterFactory implements ConverterFactory<List<String>> {

    @Override
    public Converter<List<String>> createConverter(final ExpandedQuery expandedQuery) {
        return TermListConverter.of(expandedQuery);
    }

}
