package querqy.converter.solr.map;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import querqy.QueryConfig;
import querqy.converter.Converter;
import querqy.converter.ConverterFactory;
import querqy.converter.solr.map.boost.BoostConverter;

import java.util.Map;


@Builder
public class MapConverterFactory implements ConverterFactory<Map<String, Object>> {

    @Deprecated private final MapConverterConfig converterConfig;

    // TODO: Pass expandedQuery to method convert(); keep only QueryConfig here
    // TODO: Fully remove MapConverterConfig

    @Override
    public Converter<Map<String, Object>> createConverter(final QueryConfig queryConfig) {
        return MapConverterCreator.of(queryConfig).create();
    }

    public static MapConverterFactory create() {
        return MapConverterFactory.builder()
                .converterConfig(MapConverterConfig.defaultConfig())
                .build();
    }

    @RequiredArgsConstructor(staticName = "of")
    private static class MapConverterCreator {
        private final QueryConfig queryConfig;

        public MapConverter create() {
            return MapConverter.builder()
                    .querqyQueryConverter(createQuerqyQueryMapConverter())
                    .filterConverter(createFilterMapConverter())
                    .boostConverter(createBoostConverter())
                    .build();
        }

        public QuerqyQueryConverter createQuerqyQueryMapConverter() {
            return QuerqyQueryConverter.builder()
                    .queryConfig(queryConfig)
                    .termConverter(createTermMapConverter())
                    .build();
        }

        public TermConverter createTermMapConverter() {
            return TermConverter.builder()
                    .queryConfig(queryConfig)
                    .build();
        }

        public FilterConverter createFilterMapConverter() {
            return FilterConverter.builder()
                    .querqyQueryConverter(createQuerqyQueryMapConverter())
                    .build();
        }

        public BoostConverter createBoostConverter() {
            return BoostConverter.builder()
                    .querqyQueryConverter(createQuerqyQueryMapConverter())
                    .build();
        }



    }
}
