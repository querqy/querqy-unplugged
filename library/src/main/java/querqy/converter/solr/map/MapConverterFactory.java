package querqy.converter.solr.map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import querqy.QueryConfig;
import querqy.converter.Converter;
import querqy.converter.ConverterFactory;
import querqy.converter.solr.map.boost.BoostConverter;

import java.util.Map;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MapConverterFactory implements ConverterFactory<Map<String, Object>> {

    @Override
    public Converter<Map<String, Object>> createConverter(final QueryConfig queryConfig) {
        return MapConverterCreator.of(queryConfig).create();
    }

    public static MapConverterFactory create() {
        return new MapConverterFactory();
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
