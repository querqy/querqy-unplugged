package querqy.converter.solr.map.boost;

import lombok.Builder;
import querqy.converter.solr.map.QuerqyQueryConverter;
import querqy.model.QuerqyQuery;
import querqy.model.StringRawQuery;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder
public class BoostQueryConverter {

    private static final String BOOST_UP_FUNCTION_QUERY_TEMPLATE = "{!func}mul(%f,sub(1,div(1,sum(query($%s),1))))";

    private final Collection<querqy.model.BoostQuery> boostUpQueries;
    private final Collection<querqy.model.BoostQuery> boostDownQueries;

    private final QuerqyQueryConverter querqyQueryConverter;

    private final ParameterKeyCreator parameterKeyCreator = ParameterKeyCreator.create();

    public List<ConvertedBoostQuery> convert() {
        final Stream<ConvertedBoostQuery> convertedBoostUpQueries = convertBoostUpQueries();
        final Stream<ConvertedBoostQuery> convertedBoostDownQueries = convertBoostDownQueries();
        return Stream.concat(convertedBoostUpQueries, convertedBoostDownQueries)
                .collect(Collectors.toList());
    }

    public Stream<ConvertedBoostQuery> convertBoostUpQueries() {
        return boostUpQueries.stream()
                .map(this::convertBoostUpQuery);
    }

    // TODO: depends on QueryConfig (qboost.fieldWeight on vs. off)
    private ConvertedBoostQuery convertBoostUpQuery(final querqy.model.BoostQuery boostQuery) {
        final String queryReference = parameterKeyCreator.createKey();
        final Object convertedQuery = convertQuerqyQuery(boostQuery.getQuery());

        // AdditiveBoostFunction: boostValue * (1f - (1f/(score + 1f)));
        final Object boostFunctionQuery = createBoostUpFunctionQuery(boostQuery.getBoost(), queryReference);

        return ConvertedBoostQuery.builder()
                .boostFunctionQuery(boostFunctionQuery)
                .queryReference(queryReference)
                .query(convertedQuery)
                .build();
    }

    private Object convertQuerqyQuery(final QuerqyQuery<?> querqyQuery) {
        if (querqyQuery instanceof StringRawQuery) {
            return parseStringRawQueryForBoost((StringRawQuery) querqyQuery);

        } else {
            return querqyQueryConverter.convert(querqyQuery);
        }
    }
    private String parseStringRawQueryForBoost(final StringRawQuery rawQuery) {
        return rawQuery.getQueryString();
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

    public Stream<ConvertedBoostQuery> convertBoostDownQueries() {
        return boostDownQueries.stream()
                .map(this::convertBoostDownQuery);
    }

    private ConvertedBoostQuery convertBoostDownQuery(final querqy.model.BoostQuery boostQuery) {
        throw new UnsupportedOperationException("Boost Down Queries are currently not supported");
    }
}
