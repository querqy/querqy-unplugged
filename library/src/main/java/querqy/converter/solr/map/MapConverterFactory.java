package querqy.converter.solr.map;

import lombok.Builder;
import querqy.QueryConfig;
import querqy.converter.Converter;
import querqy.converter.ConverterFactory;
import querqy.model.ExpandedQuery;

import java.util.Map;


@Builder
public class MapConverterFactory implements ConverterFactory<Map<String, Object>> {

    private final MapConverterConfig converterConfig;

    @Override
    public Converter<Map<String, Object>> createConverter(final ExpandedQuery expandedQuery, final QueryConfig queryConfig) {
        return MapConverter.builder()
                .expandedQuery(expandedQuery)
                .queryConfig(queryConfig)
                .converterConfig(converterConfig)
                .build();
    }

    public static MapConverterFactory create() {
        return MapConverterFactory.builder()
                .converterConfig(MapConverterConfig.defaultConfig())
                .build();
    }
}
