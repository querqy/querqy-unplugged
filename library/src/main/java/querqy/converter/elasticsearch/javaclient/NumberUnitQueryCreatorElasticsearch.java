package querqy.converter.elasticsearch.javaclient;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.json.JsonData;
import querqy.model.BoostQuery;
import querqy.model.Clause;
import querqy.model.RawQuery;
import querqy.rewrite.contrib.numberunit.NumberUnitQueryCreator;
import querqy.rewrite.contrib.numberunit.model.LinearFunction;
import querqy.rewrite.contrib.numberunit.model.NumberUnitDefinition;
import querqy.rewrite.contrib.numberunit.model.PerUnitNumberUnitDefinition;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NumberUnitQueryCreatorElasticsearch extends NumberUnitQueryCreator {

    public NumberUnitQueryCreatorElasticsearch(int scale) {
        super(scale);
    }


    protected RawQuery createRawBoostQuery(final BigDecimal value,
                                           final List<PerUnitNumberUnitDefinition> perUnitNumberUnitDefinitions) {

        //final BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        //final BoolQueryBuilder boolQueryBuilderLowerFilter = new BoolQueryBuilder();
        final List<Query> lowerFilterClauses = new ArrayList<>(perUnitNumberUnitDefinitions.size());
        final List<Query> upperFilterClauses = new ArrayList<>(perUnitNumberUnitDefinitions.size());

        final List<FunctionScore> lowerFilterFunctions = new ArrayList<>();
        final List<Query> exactFilterFunctions = new ArrayList<>();
        final List<FunctionScore> upperFilterFunctions = new ArrayList<>();

        perUnitNumberUnitDefinitions.forEach(perUnitDef -> {
            final NumberUnitDefinition numberUnitDef = perUnitDef.numberUnitDefinition;

            final BigDecimal standardizedValue = value.multiply(perUnitDef.multiplier);

            final BigDecimal lowerBound = subtractPercentage(standardizedValue,
                    numberUnitDef.boostPercentageLowerBoundary);

            final BigDecimal lowerBoundExactMatch = subtractPercentage(standardizedValue,
                    numberUnitDef.boostPercentageLowerBoundaryExactMatch);

            final BigDecimal upperBound = addPercentage(standardizedValue,
                    numberUnitDef.boostPercentageUpperBoundary);

            final BigDecimal upperBoundExactMatch = addPercentage(standardizedValue,
                    numberUnitDef.boostPercentageUpperBoundaryExactMatch);

            final BigDecimal lowerOrigin = standardizedValue.subtract(standardizedValue.subtract(lowerBoundExactMatch));

            final BigDecimal lowerScale = lowerBoundExactMatch.subtract(lowerBound)
                    .divide(BigDecimal.valueOf(2), super.getRoundingMode());

            final BigDecimal lowerDecay = calculateDecay(numberUnitDef.maxScoreForExactMatch,
                    numberUnitDef.minScoreAtLowerBoundary);

            final BigDecimal upperOrigin = standardizedValue.add(upperBoundExactMatch.subtract(standardizedValue));

            final BigDecimal upperScale = upperBound.subtract(upperBoundExactMatch)
                    .divide(BigDecimal.valueOf(2), super.getRoundingMode());

            final BigDecimal upperDecay = calculateDecay(numberUnitDef.maxScoreForExactMatch,
                    numberUnitDef.minScoreAtUpperBoundary);



            perUnitDef.numberUnitDefinition.fields.forEach(field -> {
                lowerFilterClauses.add(RangeQuery.of(r -> r
                                        .field(field.fieldName)
                                        .gte(JsonData.of(lowerBound.setScale(field.scale, super.getRoundingMode()).doubleValue()))
                                        .lt(JsonData.of(lowerBoundExactMatch.setScale(field.scale, super.getRoundingMode()).doubleValue()))
                                )._toQuery()
                );
                upperFilterClauses.add(
                        RangeQuery.of(r -> r
                                .field(field.fieldName)
                                .gt(JsonData.of(upperBoundExactMatch.setScale(field.scale, super.getRoundingMode()).doubleValue()))
                                .lte(JsonData.of(upperBound.setScale(field.scale, super.getRoundingMode()).doubleValue()))
                                )._toQuery()
                );

                lowerFilterFunctions.add(FunctionScore.of( f -> f.linear(
                        l -> l
                                .field(field.fieldName)
                                .placement(p -> p
                                        .origin(JsonData.of(lowerOrigin.setScale(field.scale, super.getRoundingMode()).doubleValue()))
                                        .scale(JsonData.of(lowerScale.doubleValue()))
                                        .offset(JsonData.of(0))
                                        .decay(lowerDecay.doubleValue())
                                )


                ).weight(numberUnitDef.maxScoreForExactMatch.doubleValue())));


                // String fieldName, Object origin, Object scale, Object offset, double decay
                upperFilterFunctions.add( FunctionScore.of((f -> f.linear(
                                l -> l
                                        .field(field.fieldName)
                                        .placement(p -> p
                                                .origin(JsonData.of(upperOrigin.setScale(field.scale, super.getRoundingMode()).doubleValue()))
                                                .scale(JsonData.of(upperScale.doubleValue()))
                                                .offset(JsonData.of(0))
                                                .decay(upperDecay.doubleValue())
                                        )


                        ).weight(numberUnitDef.maxScoreForExactMatch.doubleValue()))
                ));

                exactFilterFunctions.add( FunctionScoreQuery.of(fs -> fs
                                .query(q -> q.range(
                                        r -> r.field(field.fieldName)
                                                .gte(JsonData.of(lowerBoundExactMatch.setScale(field.scale, super.getRoundingMode()).doubleValue()))
                                                .lte(JsonData.of(upperBoundExactMatch.setScale(field.scale, super.getRoundingMode()).doubleValue()))
                                )).functions( f -> f.weight(
                                        numberUnitDef.maxScoreForExactMatch
                                                .add(numberUnitDef.additionalScoreForExactMatch).doubleValue()
                                        )

                                ))._toQuery()
                        );






            });
        });


        final BoolQuery boolQuery = BoolQuery.of(b -> b
                .should(
                        FunctionScoreQuery.of(fs -> fs
                                .query(BoolQuery.of(bq -> bq.should(lowerFilterClauses))._toQuery())
                                .functions(lowerFilterFunctions).boostMode(FunctionBoostMode.Multiply)
                                .scoreMode(FunctionScoreMode.Max))._toQuery())

                .should(exactFilterFunctions)
                .should(FunctionScoreQuery.of(fs -> fs
                        .query(BoolQuery.of(bq -> bq.should(upperFilterClauses))._toQuery())
                        .functions(upperFilterFunctions).boostMode(FunctionBoostMode.Multiply)
                        .scoreMode(FunctionScoreMode.Max))._toQuery()
                )
        );


        return new ElasticsearchDSLRawQuery(null, boolQuery._toQuery(), Clause.Occur.MUST, true);
    }

    @Override
    public BoostQuery createBoostQuery(final BigDecimal value,
                                       final List<PerUnitNumberUnitDefinition> perUnitNumberUnitDefinitions) {
        return new BoostQuery(createRawBoostQuery(value, perUnitNumberUnitDefinitions), 1.0f);
    }

    @Override
    public RawQuery createFilterQuery(final BigDecimal value,
                                      final List<PerUnitNumberUnitDefinition> perUnitNumberUnitDefinitions) {


        final List<Query> queries = perUnitNumberUnitDefinitions.stream().flatMap(def -> {
            final BigDecimal multipliedValue = value.multiply(def.multiplier);

            final BigDecimal lowerBound = def.numberUnitDefinition.filterPercentageLowerBoundary.compareTo(BigDecimal.ZERO) >= 0
                    ? subtractPercentage(multipliedValue, def.numberUnitDefinition.filterPercentageLowerBoundary)
                    : def.numberUnitDefinition.filterPercentageLowerBoundary;

            final BigDecimal upperBound = def.numberUnitDefinition.filterPercentageUpperBoundary.compareTo(BigDecimal.ZERO) >= 0
                    ? addPercentage(multipliedValue, def.numberUnitDefinition.filterPercentageUpperBoundary)
                    : def.numberUnitDefinition.filterPercentageUpperBoundary;

            return def.numberUnitDefinition.fields.stream().map(field ->

                            RangeQuery.of(
                                    r -> {
                                        final RangeQuery.Builder builder = r.field(field.fieldName);
                                        if (lowerBound.compareTo(BigDecimal.ZERO) >= 0) {
                                            builder.gte(JsonData.of(lowerBound.setScale(field.scale, super.getRoundingMode()).doubleValue()));
                                        }
                                        if (upperBound.compareTo(BigDecimal.ZERO) >= 0) {
                                            builder.lte(JsonData.of(upperBound.setScale(field.scale, super.getRoundingMode()).doubleValue()));
                                        }
                                        return builder;
                                    }

                            )._toQuery()

            );

        }).collect(Collectors.toUnmodifiableList());

        final Query query = BoolQuery.of(
                b -> b.minimumShouldMatch("1").
                        should(queries)
        )._toQuery();

        return new ElasticsearchDSLRawQuery(null, query, Clause.Occur.SHOULD, true);
    }

    private BigDecimal calculateDecay(BigDecimal maxValue, BigDecimal minValue) {
        final BigDecimal decayGround = minValue.divide(maxValue, super.getRoundingMode());
        final BigDecimal decaySummand = BigDecimal.ONE.subtract(decayGround)
                .divide(BigDecimal.valueOf(2), super.getRoundingMode());

        return decayGround.add(decaySummand);

    }

}
