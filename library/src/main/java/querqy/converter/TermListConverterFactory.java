package querqy.converter;

import lombok.NoArgsConstructor;
import querqy.QueryConfig;
import querqy.QueryExpansionConfig;

import java.util.List;

@NoArgsConstructor(staticName = "create")
public class TermListConverterFactory implements ConverterFactory<List<String>> {

    @Override
    public Converter<List<String>> createConverter(final QueryConfig queryConfig) {
        return TermListConverter.create();
    }

    @Override
    public Converter<List<String>> createConverter(final QueryConfig queryConfig, final QueryExpansionConfig<List<String>> queryExpansionConfig) {
        return TermListConverter.create();
    }

}
