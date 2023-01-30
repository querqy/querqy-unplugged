package querqy.converter.generic;

import lombok.Builder;
import lombok.NonNull;
import querqy.BoostConfig;
import querqy.converter.generic.builder.BoostQueryBuilder;
import querqy.converter.generic.model.BoostQueryDefinition;
import querqy.model.BoostQuery;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder
public class GenericBoostConverter<T> {

    @NonNull private final GenericQuerqyQueryConverter<T> genericQuerqyQueryConverter;
    @NonNull private final BoostQueryBuilder<T> boostQueryBuilder;

    @NonNull private final BoostConfig boostConfig;

    public List<T> convert(final Collection<BoostQuery> boostUpQueries,
                           final Collection<BoostQuery> boostDownQueries) {
        return Stream
                .concat(
                        mapToBoostQueryDefinitions(boostUpQueries)
                                .map(boostQueryBuilder::convertBoostUp),
                        mapToBoostQueryDefinitions(boostDownQueries)
                                .map(boostQueryBuilder::convertBoostDown)
                )
                .collect(Collectors.toList());
    }

    private Stream<BoostQueryDefinition<T>> mapToBoostQueryDefinitions(final Collection<BoostQuery> boostQueries) {
        if (boostQueries == null) {
            return Stream.empty();

        } else {
            return boostQueries.stream().map(this::mapToBoostQueryDefinition);
        }
    }

    private BoostQueryDefinition<T> mapToBoostQueryDefinition(final BoostQuery boostQuery) {
        return BoostQueryDefinition.<T>builder()
                .query(genericQuerqyQueryConverter.convert(boostQuery.getQuery()))
                .boostConfig(boostConfig)
                .boost(boostQuery.getBoost())
                .build();
    }
}
