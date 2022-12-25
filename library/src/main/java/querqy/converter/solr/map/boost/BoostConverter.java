package querqy.converter.solr.map.boost;

import lombok.Builder;
import querqy.converter.solr.map.QuerqyQueryConverter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
public class BoostConverter {

    private static final String BOOST_UP_FUNCTION_QUERY_TEMPLATE = "{!func}mul(%f,sub(1,div(1,sum(query($%s),1))))";

    private final QuerqyQueryConverter querqyQueryConverter;

    public ConvertedBoostQueries convertBoostQueries(
            final Collection<querqy.model.BoostQuery> boostUpQueries,
            final Collection<querqy.model.BoostQuery> boostDownQueries
    ) {
        final BoostQueryConverter boostQueryConverter = BoostQueryConverter.builder()
                .boostUpQueries(boostUpQueries)
                .boostDownQueries(boostDownQueries)
                .querqyQueryConverter(querqyQueryConverter)
                .build();

        final List<ConvertedBoostQuery> convertedBoostQueries = boostQueryConverter.convert();

        return aggregateConvertedBoostQueries(convertedBoostQueries);
    }

    private ConvertedBoostQueries aggregateConvertedBoostQueries(final List<ConvertedBoostQuery> convertedBoostQueries) {
        final List<Object> boostFunctionQueries = convertedBoostQueries.stream()
                .map(ConvertedBoostQuery::getBoostFunctionQuery)
                .collect(Collectors.toList());

        final Map<String, Object> referencedConvertedQueries = convertedBoostQueries.stream()
                .collect(
                        Collectors.toMap(
                                ConvertedBoostQuery::getQueryReference,
                                ConvertedBoostQuery::getQuery
                        )
                );

        return ConvertedBoostQueries.builder()
                .boostFunctionQueries(boostFunctionQueries)
                .referencedQueries(referencedConvertedQueries)
                .build();
    }
}
