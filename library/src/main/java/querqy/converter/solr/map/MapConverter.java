package querqy.converter.solr.map;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import querqy.QueryConfig;
import querqy.converter.Converter;
import querqy.model.ExpandedQuery;
import querqy.model.QuerqyQuery;

import java.util.Map;

import static querqy.converter.solr.map.MapConverter.InstructionQueriesParsingMode.BOOSTS_ONLY;
import static querqy.converter.solr.map.MapConverter.InstructionQueriesParsingMode.FILTERS_AND_BOOSTS;
import static querqy.converter.solr.map.MapConverter.InstructionQueriesParsingMode.FILTERS_ONLY;
import static querqy.converter.solr.map.MapConverter.InstructionQueriesParsingMode.NO_INSTRUCTION_QUERY;

@RequiredArgsConstructor(staticName = "of", access = AccessLevel.PROTECTED)
public class MapConverter implements Converter<Map<String, Object>> {

    public enum InstructionQueriesParsingMode {
        NO_INSTRUCTION_QUERY, FILTERS_ONLY, BOOSTS_ONLY, FILTERS_AND_BOOSTS
    }

    private final QueryConfig queryConfig;
    private final MapConverterConfig converterConfig;
    private final QuerqyQuery<?> userQuery;
    private final FilterMapConverter filterMapConverter;
    private final BoostMapConverter boostMapConverter;

    @Builder
    public static MapConverter build(
            final ExpandedQuery expandedQuery,
            final QueryConfig queryConfig,
            final MapConverterConfig converterConfig
    ) {
        final FilterMapConverter filterMapConverter = FilterMapConverter.builder()
                .queryConfig(queryConfig)
                .converterConfig(converterConfig)
                .filterQueries(expandedQuery.getFilterQueries())
                .build();

        final BoostMapConverter boostMapConverter = BoostMapConverter.builder()
                .queryConfig(queryConfig)
                .boostUpQueries(expandedQuery.getBoostUpQueries())
                .boostDownQueries(expandedQuery.getBoostDownQueries())
                .build();

        return MapConverter.of(
                queryConfig,
                converterConfig,
                expandedQuery.getUserQuery(),
                filterMapConverter,
                boostMapConverter
        );
    }

    @Override
    public Map<String, Object> convert() {
        final Map<String, Object> convertedUserQuery = convertUserQuery();

        final InstructionQueriesParsingMode parsingMode = getParsingMode();

        return expandUserQuery(convertedUserQuery, parsingMode);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertUserQuery() {
        final Object query =  QuerqyQueryMapConverter.builder()
                .queryConfig(queryConfig)
                .converterConfig(converterConfig)
                .node(userQuery)
                .parseAsUserQuery(true)
                .build()
                .convert();

        // TODO: Why is this needed?
        //  -> raw query not possible for user query
        //  -> match all query not identified by querqy core
        if (query instanceof Map) {
            return (Map<String, Object>) query;

        } else if (query instanceof String){
            return Map.of("bool", Map.of("must", (String) query));

        } else {
            throw new IllegalArgumentException("Converted user query must be of type Map or String");
        }
    }

    private InstructionQueriesParsingMode getParsingMode() {
        if (filterMapConverter.hasFilters()) {
            return boostMapConverter.hasBoosts() ? FILTERS_AND_BOOSTS : FILTERS_ONLY;

        } else {
            return boostMapConverter.hasBoosts() ? BOOSTS_ONLY : NO_INSTRUCTION_QUERY;
        }
    }



    private Map<String, Object> expandUserQuery(final Object convertedUserQuery,
                                                final InstructionQueriesParsingMode parsingMode) {
        switch (parsingMode) {
            case NO_INSTRUCTION_QUERY:
                return Map.of("query", convertedUserQuery);

            case FILTERS_ONLY:
                return expandByFiltersOnly(convertedUserQuery);

            case BOOSTS_ONLY:
                return expandByBoostsOnly(convertedUserQuery);

            default:
                return expandByFiltersAndBoosts(convertedUserQuery);
        }
    }

    private Map<String, Object> expandByFiltersOnly(final Object convertedUserQuery) {
        return Map.of(
                "query", Map.of(
                        "bool", Map.of(
                                "must", convertedUserQuery
                        )
                ),
                "filter", filterMapConverter.convertFilterQueries()
        );
    }

    private Map<String, Object> expandByBoostsOnly(final Object convertedUserQuery) {
        final BoostMaps boostMaps = boostMapConverter.convertBoostQueries();

        return Map.of(
                "query", Map.of(
                        "bool", Map.of(
                                "must", convertedUserQuery,
                                "should", boostMaps.getBoostFunctionQueries()
                        )
                ),
                "queries", boostMaps.getReferencedConvertedQueries()
        );
    }

    private Map<String, Object> expandByFiltersAndBoosts(final Object convertedUserQuery) {
        final BoostMaps boostMaps = boostMapConverter.convertBoostQueries();

        return Map.of(
                "query", Map.of(
                        "bool", Map.of(
                                "must", convertedUserQuery,
                                "should", boostMaps.getBoostFunctionQueries()
                        )
                ),
                "filter", filterMapConverter.convertFilterQueries(),
                "queries", boostMaps.getReferencedConvertedQueries()
        );
    }
}
