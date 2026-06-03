package querqy.converter.opensearch.javaclient;

import org.opensearch.client.opensearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import querqy.QueryConfig;
import querqy.QueryExpansionConfig;
import querqy.converter.Converter;
import querqy.converter.ConverterFactory;
import querqy.converter.opensearch.javaclient.builder.OSJavaClientBooleanQueryBuilder;
import querqy.converter.opensearch.javaclient.builder.OSJavaClientBoostQueryBuilder;
import querqy.converter.opensearch.javaclient.builder.OSJavaClientConstantScoreQueryBuilder;
import querqy.converter.opensearch.javaclient.builder.OSJavaClientDismaxQueryBuilder;
import querqy.converter.opensearch.javaclient.builder.OSJavaClientMatchAllQueryBuilder;
import querqy.converter.opensearch.javaclient.builder.OSJavaClientQueryStringQueryBuilder;
import querqy.converter.opensearch.javaclient.builder.OSJavaClientRawQueryBuilder;
import querqy.converter.opensearch.javaclient.builder.OSJavaClientTermQueryBuilder;
import querqy.converter.generic.GenericConverterFactory;

@RequiredArgsConstructor
public class OSJavaClientConverterFactory implements ConverterFactory<Query> {

    private final OSJavaClientConverterConfig converterConfig;

    private final OSJavaClientBooleanQueryBuilder booleanQueryBuilder = OSJavaClientBooleanQueryBuilder.create();
    private final OSJavaClientDismaxQueryBuilder dismaxQueryBuilder = OSJavaClientDismaxQueryBuilder.create();
    private final OSJavaClientConstantScoreQueryBuilder constantScoreQueryBuilder = OSJavaClientConstantScoreQueryBuilder.create();
    private final OSJavaClientTermQueryBuilder termQueryBuilder = OSJavaClientTermQueryBuilder.create();
    private final OSJavaClientMatchAllQueryBuilder matchAllQueryBuilder = OSJavaClientMatchAllQueryBuilder.create();
    private final OSJavaClientBoostQueryBuilder boostQueryBuilder = OSJavaClientBoostQueryBuilder.create();
    private final OSJavaClientQueryStringQueryBuilder queryStringQueryBuilder = OSJavaClientQueryStringQueryBuilder.create();

    @Override
    public Converter<Query> createConverter(final QueryConfig queryConfig, final QueryExpansionConfig<Query> queryExpansionConfig) {
        final OSJavaClientRawQueryBuilder rawQueryBuilder = OSJavaClientRawQueryBuilder.of(converterConfig);

        final GenericConverterFactory<Query> converterFactory = GenericConverterFactory.<Query>builder()
                .booleanQueryBuilder(booleanQueryBuilder)
                .dismaxQueryBuilder(dismaxQueryBuilder)
                .constantScoreQueryBuilder(constantScoreQueryBuilder)
                .termQueryBuilder(termQueryBuilder)
                .matchAllQueryBuilder(matchAllQueryBuilder)
                .rawQueryBuilder(rawQueryBuilder)
                .boostQueryBuilder(boostQueryBuilder)
                .queryStringQueryBuilder(queryStringQueryBuilder)
                .build();

        return converterFactory.createConverter(queryConfig, queryExpansionConfig);
    }

    public static OSJavaClientConverterFactory create() {
        return new OSJavaClientConverterFactory(OSJavaClientConverterConfig.defaultConfig());
    }

    public static OSJavaClientConverterFactory of(final OSJavaClientConverterConfig clientConverterConfig) {
        return new OSJavaClientConverterFactory(clientConverterConfig);
    }
}
