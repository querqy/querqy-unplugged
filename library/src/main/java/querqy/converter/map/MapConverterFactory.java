package querqy.converter.map;

import lombok.NoArgsConstructor;
import querqy.QueryConfig;
import querqy.converter.Converter;
import querqy.converter.ConverterFactory;
import querqy.model.ExpandedQuery;

import java.util.Map;

@NoArgsConstructor(staticName = "create")
public class MapConverterFactory implements ConverterFactory<Map<String, Object>> {

    @Override
    public Converter<Map<String, Object>> createConverter(ExpandedQuery expandedQuery, QueryConfig queryConfig) {
        return MapConverter.builder()
                .expandedQuery(expandedQuery)
                .queryConfig(queryConfig)
                .build();
    }
}
