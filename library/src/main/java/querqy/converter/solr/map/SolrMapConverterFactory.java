package querqy.converter.solr.map;

import lombok.RequiredArgsConstructor;
import querqy.QueryConfig;
import querqy.QueryExpansionConfig;
import querqy.QueryTypeConfig;
import querqy.converter.Converter;
import querqy.converter.ConverterFactory;
import querqy.converter.generic.GenericConverterFactory;
import querqy.converter.solr.map.builder.SolrMapBooleanQueryBuilder;
import querqy.converter.solr.map.builder.SolrMapBoostQueryBuilder;
import querqy.converter.solr.map.builder.SolrMapConstantScoreQueryBuilder;
import querqy.converter.solr.map.builder.SolrMapDismaxQueryBuilder;
import querqy.converter.solr.map.builder.SolrMapMatchAllQueryBuilder;
import querqy.converter.solr.map.builder.SolrMapQueryReferenceBuilder;
import querqy.converter.solr.map.builder.SolrMapQueryStringQueryBuilder;
import querqy.converter.solr.map.builder.SolrMapRawQueryBuilder;
import querqy.converter.solr.map.builder.SolrMapTermQueryBuilder;
import querqy.converter.solr.map.builder.SolrMapWrappedQueryBuilder;

import java.util.Map;

@RequiredArgsConstructor(staticName = "create")
public class SolrMapConverterFactory implements ConverterFactory<Map<String, Object>> {

    public static final String DEFAULT_CONSTANT_SCORE_QUERY_TYPE_NAME = "constantScore";

    public static final QueryTypeConfig DEFAULT_TERM_QUERY_TYPE_CONFIG = QueryTypeConfig.builder()
            .typeName("field")
            .queryParamName("query")
            .fieldParamName("f")
            .build();

    private final SolrMapBooleanQueryBuilder booleanQueryBuilder = SolrMapBooleanQueryBuilder.create();
    private final SolrMapDismaxQueryBuilder dismaxQueryBuilder = SolrMapDismaxQueryBuilder.create();
    private final SolrMapMatchAllQueryBuilder matchAllQueryBuilder = SolrMapMatchAllQueryBuilder.create();

    private final SolrMapConstantScoreQueryBuilder constantScoreQueryBuilder = SolrMapConstantScoreQueryBuilder.of(DEFAULT_CONSTANT_SCORE_QUERY_TYPE_NAME);
    private final SolrMapTermQueryBuilder termQueryBuilder = SolrMapTermQueryBuilder.of(DEFAULT_TERM_QUERY_TYPE_CONFIG);

    @Override
    public Converter<Map<String, Object>> createConverter(QueryConfig queryConfig, QueryExpansionConfig<Map<String, Object>> queryExpansionConfig) {
        final SolrMapQueryReferenceBuilder queryReferenceBuilder = SolrMapQueryReferenceBuilder.create();

        final SolrMapRawQueryBuilder rawQueryBuilder = SolrMapRawQueryBuilder.of(queryReferenceBuilder);
        final SolrMapBoostQueryBuilder boostQueryBuilder = SolrMapBoostQueryBuilder.create(queryReferenceBuilder);
        final SolrMapWrappedQueryBuilder wrappedQueryBuilder = SolrMapWrappedQueryBuilder.of(queryReferenceBuilder);
        final SolrMapQueryStringQueryBuilder queryStringQueryBuilder = SolrMapQueryStringQueryBuilder.of(queryReferenceBuilder);

        final GenericConverterFactory<Map<String, Object>> converterFactory = GenericConverterFactory.<Map<String, Object>>builder()
                .booleanQueryBuilder(booleanQueryBuilder)
                .dismaxQueryBuilder(dismaxQueryBuilder)
                .constantScoreQueryBuilder(constantScoreQueryBuilder)
                .termQueryBuilder(termQueryBuilder)
                .matchAllQueryBuilder(matchAllQueryBuilder)
                .rawQueryBuilder(rawQueryBuilder)
                .boostQueryBuilder(boostQueryBuilder)
                .queryStringQueryBuilder(queryStringQueryBuilder)
                .wrappedQueryBuilder(wrappedQueryBuilder)
                .build();

        return converterFactory.createConverter(queryConfig, queryExpansionConfig);
    }
}
