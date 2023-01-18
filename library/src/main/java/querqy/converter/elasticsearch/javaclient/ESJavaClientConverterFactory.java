package querqy.converter.elasticsearch.javaclient;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import querqy.QueryConfig;
import querqy.converter.Converter;
import querqy.converter.ConverterFactory;
import querqy.converter.elasticsearch.javaclient.builder.ESJavaClientBooleanQueryBuilder;
import querqy.converter.elasticsearch.javaclient.builder.ESJavaClientDismaxQueryBuilder;
import querqy.converter.elasticsearch.javaclient.builder.ESJavaClientTermQueryBuilder;
import querqy.converter.generic.GenericConverterFactory;

@RequiredArgsConstructor(staticName = "create")
public class ESJavaClientConverterFactory implements ConverterFactory<Query> {

    private final ESJavaClientBooleanQueryBuilder booleanQueryBuilder = ESJavaClientBooleanQueryBuilder.create();
    private final ESJavaClientDismaxQueryBuilder dismaxQueryBuilder = ESJavaClientDismaxQueryBuilder.create();
    private final ESJavaClientTermQueryBuilder termQueryBuilder = ESJavaClientTermQueryBuilder.create();

    private final GenericConverterFactory<Query> converterFactory = GenericConverterFactory.<Query>builder()
            .booleanQueryBuilder(booleanQueryBuilder)
            .dismaxQueryBuilder(dismaxQueryBuilder)
            .termQueryBuilder(termQueryBuilder)
            .build();

    @Override
    public Converter<Query> createConverter(final QueryConfig queryConfig) {
        return converterFactory.createConverter(queryConfig);
    }
}
