package querqy.converter.solr.map;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import querqy.QueryConfig;
import querqy.model.BoostQuery;
import querqy.model.QuerqyQuery;
import querqy.model.StringRawQuery;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
public class BoostMapConverter {

    private static final String BOOST_UP_FUNCTION_QUERY_TEMPLATE = "{!func}mul(%f,sub(1,div(1,sum($%s,1))))";

    private final QueryConfig queryConfig;

    private final Collection<BoostQuery> boostUpQueries;
    private final Collection<BoostQuery> boostDownQueries;

    private final ParameterKeyCreator parameterKeyCreator = ParameterKeyCreator.create();

    @Builder
    public static BoostMapConverter build(final QueryConfig queryConfig,
                                          final Collection<BoostQuery> boostUpQueries,
                                          final Collection<BoostQuery> boostDownQueries) {
        return BoostMapConverter.of(
                queryConfig,
                boostUpQueries == null ? List.of() : boostUpQueries,
                boostDownQueries == null ? List.of() : boostDownQueries
        );
    }

    public boolean hasBoosts() {
        return hasBoostUpQueries() || hasBoostDownQueries();
    }

    public boolean hasBoostUpQueries() {
        return boostUpQueries.size() > 0;
    }

    public boolean hasBoostDownQueries() {
        return boostDownQueries.size() > 0;
    }

    public BoostMaps convertBoostQueries() {
        final BoostMaps boostMaps = BoostMaps.create();

        final Stream<BoostMapEntry> convertedBoostUpQueries = convertBoostUpQueries();
        final Stream<BoostMapEntry> convertedBoostDownQueries = convertBoostDownQueries();
        Stream.concat(convertedBoostUpQueries, convertedBoostDownQueries)
                .forEach(boostMaps::addBoostMapTuple);

        return boostMaps;
    }

    public Stream<BoostMapEntry> convertBoostUpQueries() {
        return boostUpQueries.stream()
                .map(this::convertBoostUpQuery);
    }

    // TODO: depends on QueryConfig (qboost.fieldWeight on vs. off)
    private BoostMapEntry convertBoostUpQuery(final BoostQuery boostQuery) {
        final String queryReference = parameterKeyCreator.createKey();
        final Object convertedQuery = convertQuerqyQuery(boostQuery.getQuery());

        // AdditiveBoostFunction: boostValue * (1f - (1f/(score + 1f)));
        final Object boostFunctionQuery = createBoostUpFunctionQuery(boostQuery.getBoost(), queryReference);

        return BoostMapEntry.builder()
                .boostFunctionQuery(boostFunctionQuery)
                .convertedQueryReference(queryReference)
                .convertedQuery(convertedQuery)
                .build();
    }

    private Object convertQuerqyQuery(final QuerqyQuery<?> querqyQuery) {
        if (querqyQuery instanceof StringRawQuery) {
            return parseStringRawQueryForBoost((StringRawQuery) querqyQuery);

        } else {
            return QuerqyQueryMapConverter.builder()
                    .queryConfig(queryConfig)
                    .node(querqyQuery)
                    .parseAsUserQuery(false)
                    .build()
                    .convert();
        }
    }

    private String parseStringRawQueryForBoost(final StringRawQuery rawQuery) {
        final String rawQueryString = rawQuery.getQueryString();

        if (rawQueryString.startsWith("{!")) {
            return rawQueryString;

        } else {
            return "{!lucene}" + rawQueryString;
        }
    }

    private Object createBoostUpFunctionQuery(final float boost, final String queryReference) {
        if (boost == 1) {
            return Map.of("param", queryReference);

        } else {
            return String.format(
                    Locale.US,
                    BOOST_UP_FUNCTION_QUERY_TEMPLATE,
                    boost,
                    queryReference
            );
        }
    }

    public Stream<BoostMapEntry> convertBoostDownQueries() {
        return boostDownQueries.stream()
                .map(this::convertBoostDownQuery);
    }

    private BoostMapEntry convertBoostDownQuery(final BoostQuery boostQuery) {
        throw new UnsupportedOperationException("Boost Down Queries are currently not supported");
    }

    @NoArgsConstructor(staticName = "create")
    private static class ParameterKeyCreator {
        private static final String PARAM_PREFIX = "_querqy_boost_";

        private int parameterCount = 0;

        public String createKey() {
            return PARAM_PREFIX + parameterCount++;
        }
    }
}
