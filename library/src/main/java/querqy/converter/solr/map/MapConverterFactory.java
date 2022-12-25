package querqy.converter.solr.map;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import querqy.QueryConfig;
import querqy.converter.Converter;
import querqy.converter.ConverterFactory;
import querqy.converter.solr.map.boost.BoostConverter;
import querqy.model.ExpandedQuery;

import java.util.Map;


@Builder
public class MapConverterFactory implements ConverterFactory<Map<String, Object>> {

    private final MapConverterConfig converterConfig;


    // TODO: Pass expandedQuery to method convert(); keep only QueryConfig here
    // TODO: Fully remove MapConverterConfig

    @Override
    public Converter<Map<String, Object>> createConverter(final ExpandedQuery expandedQuery, final QueryConfig queryConfig) {
        return MapConverterCreator.of(queryConfig, converterConfig, expandedQuery).create();
    }

    public static MapConverterFactory create() {
        return MapConverterFactory.builder()
                .converterConfig(MapConverterConfig.defaultConfig())
                .build();
    }

    @RequiredArgsConstructor(staticName = "of")
    private static class MapConverterCreator {
        private final QueryConfig queryConfig;
        private final MapConverterConfig converterConfig;
        @Deprecated private final ExpandedQuery expandedQuery;

        public MapConverter create() {
            return MapConverter.builder()
                    .expandedQuery(expandedQuery)
                    .queryConfig(queryConfig)
                    .converterConfig(converterConfig)
                    .querqyQueryConverter(createQuerqyQueryMapConverter(true))
                    .filterConverter(createFilterMapConverter())
                    .boostConverter(createBoostConverter())
                    .build();
        }

        public QuerqyQueryConverter createQuerqyQueryMapConverter(final boolean parseAsUserQuery) {
            return QuerqyQueryConverter.builder()
                    .queryConfig(queryConfig)
                    .converterConfig(converterConfig)
                    .parseAsUserQuery(parseAsUserQuery)
                    .termConverter(createTermMapConverter())
                    .build();
        }

        public TermConverter createTermMapConverter() {
            return TermConverter.builder()
                    .queryConfig(queryConfig)
                    .converterConfig(converterConfig)
                    .build();
        }

        public FilterConverter createFilterMapConverter() {
            return FilterConverter.builder()
                    .querqyQueryConverter(createQuerqyQueryMapConverter(false))
                    .build();
        }

        public BoostConverter createBoostConverter() {
            return BoostConverter.builder()
                    .querqyQueryConverter(createQuerqyQueryMapConverter(false))
                    .build();
        }



    }
}
