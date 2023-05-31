# Querqy-Unplugged

## Overview

Querqy-Unplugged is a library that facilitates to apply business rules in a search solution. It fully bases on 
Querqy, a query-rewriting plugin for Solr, Elasticsearch and OpenSearch that is used by various companies in the world, 
predominantly retailers. However, in contrast to using Querqy as a plugin, Querqy-Unplugged does not require customized 
deployments of a search engine as it is able to interact with the vanilla releases. The library can be included into a 
microservice on top of the search engine in order to abstract the processing and the deployment of business features away 
from challenges regarding hosting or scaling the search engine. Furthermore, this approach is better suitable for using a 
search engine as a managed service in order to enable engineers and scientists to fully focus on implementing business 
features. 

Fundamentals of query-rewriting are explained [here](https://opensourceconnections.com/blog/2021/10/19/fundamentals-of-query-rewriting-part-1-introduction-to-query-expansion/). 

## Setup

Querqy-Unplugged can be included as a dependency via the public Maven repository, e.g. using Gradle:
`implementation 'org.querqy:querqy-unplugged:0.13.0'`.

## Usage

Querqy-Unplugged currently is able to interact with Elasticsearch via its [Java API Client](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/7.17/index.html)
and with Solr via its [JSON Query DSL](https://solr.apache.org/guide/solr/latest/query-guide/json-query-dsl.html). However, the 
library is implemented in a way to minimize efforts to add support for additional approaches (e.g. Elasticsearch Query DSL)
and search engines (e.g. OpenSearch). 

The basic usage of Querqy-Unplugged requires three components: 

* a Querqy configuration (e.g. rewriters),
* a query configuration (e.g. fields) and 
* a converter. 

* The processing is applied in a way that the query is rewritten by Querqy into a Querqy tree structure and subsequently
transformed by the given converter to an output that can be applied to query a certain search engine using the query 
configuration. Therefore, the querqy and the query configuration are used in the same way across search engines whereas
only the converter is search engine-specific. 

The three components are used by passing them to class `QueryRewriting` as shown above:
```java
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

final QueryRewriting<Query> queryRewriting = QueryRewriting.<Query>builder()
        .querqyConfig(querqyConfig)
        .queryConfig(queryConfig)
        .converterFactory(converterFactory)
        .build();

final Query query = queryRewriting.rewriteQuery("iphone").getConvertedQuery();
```

The object `queryRewriting` ist stateless and can be reused for different queries. 

The subsequent examples will be based on the query input `iphone` with a single common rules rewriter including the rule
`iphone => \n SYNONYM: apple smartphone`.

### Querqy Configuration

Querqy configurations include a parser definition and rewriters. A Querqy configuration can be created as follows:
```java
QuerqyConfig.builder()
        .replaceRules(
                ReplaceRulesDefinition.builder()
                    .rewriterId("id1")
                    .rules("aple => apple")
                    .build()
        )
        .commonRules(
                CommonRulesDefinition.builder()
                    .rewriterId("id1")
                    .rules("iphone => \n SYNONYM: apple smartphone")
                    .build()
        )
    .build();
```

As a default, the WhitespaceQuerqyParserFactory will be added to the config, which splits incoming query strings by the 
different kinds of whitespaces. This should be the best option for most applications.

The QuerqyParser will first create a Querqy tree object representation from the input, which corresponds to the following 
structure (human-readable representation):
```
bool(
  dismax(
    term(iphone)
  )
)
```

Subsequently, the rewriters will be applied, enhancing the object representation by a synonym:
```
bool(
  dismax(
    term(iphone)
    bool(
      dismax(
        term(apple)
      )
      dismax(
        term(smartphone)
      )
    )
  )
)
```

### Query Configuration
The query configuration includes field configurations such as field names or weights, as well as settings
related to matching and scoring (e.g. tie and minimum-should-match).
The rewriting part of Querqy mentioned above is (mostly) field-agnostic, the field-specific part is (mostly) done 
subsequently to the rewriting part and can be configured via the query configuration. A query configuration can be created 
as follows:

```java
QueryConfig.builder()
        .field("name", 40.0f)
        .field("type", 20.0f)
        .minimumShouldMatch("100%")
        .tie(0.0f)
        .build();
```

The converter will use the query config to transform the Querqy tree representation to an output that is suitable for 
the respective search engine. The converted query looks as follows:

```
bool(
  should(
    dismax(
      term(name:iphone)
      term(type:iphone)
      tie: 0.0f
    )
    dismax(
      bool(
        must(
          dismax(
            term(name:apple)
            term(type:apple)
            tie: 0.0f
          )
          dismax(
            term(name:smartphone)
            term(type:smartphone)
            tie: 0.0f
          )
        )
      )
    )
    minimumShouldMatch: 100%
  )
)
```

Be aware that the representation above is simplified, as scoring implications of nesting boolean queries are not 
considered. 

Notice that fields either can be configured in the direct way (as above) by passing a field name and a weight or more 
specifically by creating a field config passing a query type config (e.g. for defining a specific Solr query parser for a field).

#### Boost configuration

Querqy rules can include boosts. The subsequent rule pushes all apple products for queries containing `iphone`:

```text
iphone =>
  UP(10): apple
```

The query configuration can be enhanced by a boost configuration, which defines the way how boost scores are handled:

```java
QueryConfig.builder()
        .field("name", 40.0f)
        .field("type", 20.0f)
        .minimumShouldMatch("100%")
        .tie(0.0f)
        .boostConfig(
                BoostConfig.builder()
                    .boostMode(BoostConfig.QueryScoreConfig.ADD_TO_BOOST_PARAM)
                    .build()
        )
        .build();
```

There are four boost modes:

*QueryScoreConfig.IGNORE_QUERY_SCORE (default)*

Only the score defined in the parameter of the boost rule is added to the result. Given the term `iphone` matches in the
field `name`, the product gets a basic score of `40`. If the term `apple` additionally matches anywhere, an additional score
of `10` is added.

*QueryScoreConfig.ADD_TO_BOOST_PARAM*

The score of the parameter is added in addition to the score of the boosting query. If the term `apple` matches in 
the field `type`, an additional score of `30` (`20` boosting query score, `10` parameter score) is added.

*QueryScoreConfig.MULTIPLY_WITH_BOOST_PARAM*

The score of the parameter is multiplied by the score of the boosting query. If the term `apple` matches in 
the field `type`, an additional score of `200` (`20` boosting query score, `10` parameter score) is added.

*QueryScoreConfig.CLASSIC*

This mode aims to achieve a backwards-compatible boost scoring to Querqy as a plugin. However, this mode is currently only 
supported for the SolrMap client.


### QueryExpansion Configuration
Several use cases might require to enhance a query irrespective of rules or rewriters. Such enhancements can be configured 
via the `QueryExpansionConfig`. The easiest way to add queries is to add them as strings. For the Elasticsearch Java Client,
the syntax must be compatible to query string queries (which are built under the hood). For the SolrMap Client, the syntax must be 
compatible to the lucene query parser.

```java
final QueryExpansionConfig.<Query>builder()
        .addAlternativeMatchingStringQuery("id:123", 50f)
        .addBoostUpStringQuery("brand:apple", 50f)
        .filterStringQuery("type:smartphone")
        .build()
```

Currently, three types of query expansions are supported:

*Filters* are added within a bool query in addition to the querqy query.

```
bool(
  must(
    bool(
      dismax(...)
    )
  )
  filter(
    query-expansion-filter-query()
  )
)
```

*Boosts* are added within a bool query as should clauses in addition to the querqy query.

```
bool(
  must(
    bool(
      dismax(...)
    )
  )
  should(
    query-expansion-boost-query()
  )
)
```

*Alternative matching queries* are fully qualified alternatives to the querqy query, for instance to include a product with 
a certain id into the results that is not included in the regular query. The original querqy query and the alternative
matching queries are combined as should clauses in an additional bool layer (notice that the subsequent query also includes a 
query expansion boost query for demonstration purposes).

```
bool(
  should(
    bool(
      must(
        bool(
          dismax(...)
        )
      )
      should(
        query-expansion-boost-query()
      )
    )
    query-expansion-alternative-matching-query()
  )
)
```


For the case that the string based queries are not sufficient, there is the additional option to include them as query 
objects. 

```java
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;

final QueryExpansionConfig.<Query>builder()
        .addBoostUpQuery(
                new Query(
                        new TermQuery.Builder()
                            .field("brand")
                            .value("apple")
                            .build()
                ),
                50f
        )
        .build()
```

### Converters
Converters are the search engine-specific part of Querqy-Unplugged. Converters are created for each query separately via 
a factory. The classes related to converters as well as the class `QueryRewriting` make use of generic types, and the
type needs to be specified depending on the converter that is used. 

#### MapConverter for Solr

The output of the `MapConverter` for Solr is a Java Map that makes use of Solr's JSON Query DSL:
```java
final MapConverterFactory converterFactory = MapConverterFactory.create();

final QueryRewriting<Map<String, Object>> queryRewriting = QueryRewriting.<Map<String, Object>>builder()
        .querqyConfig(querqyConfig)
        .queryConfig(queryConfig)
        .converterFactory(converterFactory)
        .build();
```

Unfortunately, the features of the current JSON Query DSL are not sufficient to cover the full external query rewriting.
Therefore, the following very lightweight package including a few query parsers must be included into the deployment of
Solr: `implementation 'org.querqy:querqy-unplugged-solr:0.3.0'`

Furthermore, the following snippet must be included under the `config` node in `solrconfig.xml`:

```xml
<config>
    <queryParser name="bool" class="solr.qparser.BoolQParserWrapperPlugin"/>
    <queryParser name="nestedDismax" class="solr.qparser.NestedDisMaxQParserPlugin"/>
    <queryParser name="constantScore" class="solr.qparser.ConstantScoreQParserPlugin"/>
    <queryParser name="field" class="solr.qparser.FieldQParserPluginPatch"/>
</config>
```

However, it is planned to contribute this little enhancement to Solr in order to allow using a vanilla deployment.

#### Elasticsearch Java API Client Converter

The converter for Elasticsearch can be created and used as follows:
```java
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

final ConverterFactory<Query> converterFactory = ESJavaClientConverterFactory.create();

final QueryRewriting<Query> queryRewriting = QueryRewriting.<Query>builder()
        .querqyConfig(querqyConfig)
        .queryConfig(queryConfig)
        .converterFactory(converterFactory)
        .build();
```

Using this converter requires including the dependency for the client as Querqy-Unplugged only includes it as `compileOnly`. 


#### Implementing additional converters

Most of the converter logic is implemented using Java generics. Adding additional converters can be done by implementing these
[interfaces](https://github.com/querqy/querqy-unplugged/tree/main/library/src/main/java/querqy/converter/generic/builder)
and by creating a converter factory implementing this [interface](https://github.com/querqy/querqy-unplugged/blob/main/library/src/main/java/querqy/converter/ConverterFactory.java).  

## Limitations

### Limitations regarding the approach of rewriting without a plugin 
* In contrast to Querqy as a plugin, the library does not directly create Lucene query objects, therefore it is not able
  to make use of custom Lucene classes. Therefore, features like term frequency faking are not supported (and won't be
  supported in the future).

### Current limitations of the Querqy-Unplugged library
* The current implementation of Querqy-Unplugged eliminates all effects of term frequencies and inversed document 
  frequencies. Querqy is mostly used in the context of retail, where such effects are commonly unsuitable.
* The current Wordbreak rewriter implementation in Querqy requires access to a Lucene index. This needs to be replaced 
  by an in-memory rewriter, which has not happened so far.
* Negative boosts (`DOWN`-instructions) are not or only partially supported. However, if you need to "punish" products for certain 
  attributes, you can simply negate the boost query and use an `UP`-instruction.
