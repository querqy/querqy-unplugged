package querqy.converter.opensearch.javaclient;

import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import querqy.QueryConfig;
import querqy.QueryExpansionConfig;
import querqy.converter.Converter;
import querqy.converter.ConverterFactory;
import querqy.converter.elasticsearch.javaclient.ESJavaClientConverterConfig;
import querqy.converter.elasticsearch.javaclient.builder.ESJavaClientBooleanQueryBuilder;
import querqy.converter.elasticsearch.javaclient.builder.ESJavaClientBoostQueryBuilder;
import querqy.converter.elasticsearch.javaclient.builder.ESJavaClientConstantScoreQueryBuilder;
import querqy.converter.elasticsearch.javaclient.builder.ESJavaClientDismaxQueryBuilder;
import querqy.converter.elasticsearch.javaclient.builder.ESJavaClientMatchAllQueryBuilder;
import querqy.converter.elasticsearch.javaclient.builder.ESJavaClientQueryStringQueryBuilder;
import querqy.converter.elasticsearch.javaclient.builder.ESJavaClientRawQueryBuilder;
import querqy.converter.elasticsearch.javaclient.builder.ESJavaClientTermQueryBuilder;
import querqy.converter.generic.GenericConverterFactory;

@RequiredArgsConstructor
public class OSJavaClientConverterFactory implements ConverterFactory<Query> {

    private final ESJavaClientConverterConfig converterConfig;

    private final ESJavaClientBooleanQueryBuilder booleanQueryBuilder = ESJavaClientBooleanQueryBuilder.create();
    private final ESJavaClientDismaxQueryBuilder dismaxQueryBuilder = ESJavaClientDismaxQueryBuilder.create();
    private final ESJavaClientConstantScoreQueryBuilder constantScoreQueryBuilder = ESJavaClientConstantScoreQueryBuilder.create();
    private final ESJavaClientTermQueryBuilder termQueryBuilder = ESJavaClientTermQueryBuilder.create();
    private final ESJavaClientMatchAllQueryBuilder matchAllQueryBuilder = ESJavaClientMatchAllQueryBuilder.create();
    private final ESJavaClientBoostQueryBuilder boostQueryBuilder = ESJavaClientBoostQueryBuilder.create();
    private final ESJavaClientQueryStringQueryBuilder queryStringQueryBuilder = ESJavaClientQueryStringQueryBuilder.create();

    @Override
    public Converter<Query> createConverter(final QueryConfig queryConfig, final QueryExpansionConfig<Query> queryExpansionConfig) {
//        final ESJavaClientRawQueryBuilder rawQueryBuilder = ESJavaClientRawQueryBuilder.of(converterConfig);
//
//        final GenericConverterFactory<Query> converterFactory = GenericConverterFactory.<Query>builder()
//                .booleanQueryBuilder(booleanQueryBuilder)
//                .dismaxQueryBuilder(dismaxQueryBuilder)
//                .constantScoreQueryBuilder(constantScoreQueryBuilder)
//                .termQueryBuilder(termQueryBuilder)
//                .matchAllQueryBuilder(matchAllQueryBuilder)
//                .rawQueryBuilder(rawQueryBuilder)
//                .boostQueryBuilder(boostQueryBuilder)
//                .queryStringQueryBuilder(queryStringQueryBuilder)
//                .build();
//
//        return converterFactory.createConverter(queryConfig, queryExpansionConfig);
//
        return null;
    }

    public static OSJavaClientConverterFactory create() {
        return new OSJavaClientConverterFactory(ESJavaClientConverterConfig.defaultConfig());
    }

    public static OSJavaClientConverterFactory of(final ESJavaClientConverterConfig clientConverterConfig) {
        return new OSJavaClientConverterFactory(clientConverterConfig);
    }
}
