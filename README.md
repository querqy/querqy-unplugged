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




## Usage

Querqy-Unplugged currently is able to interact with Elasticsearch via its [Java API Client](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/7.17/index.html)
and with Solr via its [JSON Query DSL](https://solr.apache.org/guide/solr/latest/query-guide/json-query-dsl.html). However, the 
library is implemented in a way to minimize efforts to add support for additional approaches (e.g. Elasticsearch Query DSL)
and search engines (e.g. OpenSearch). 

The basic usage of Querqy-Unplugged requires three components: a Querqy configuration (e.g. rewriters), 
a query configuration (e.g. fields) and a converter. The 
processing is applied in a way that the query is rewritten by Querqy into a Querqy tree structure and subsequently
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


### Converters
Converters are the search engine-specific part of Querqy-Unplugged. Converters are created for each query separately via 
a factory. The classes related to converters as well as the class `QueryRewriting` make use of generic types, and the
type needs to be specified depending on the converter that is used. 

#### MapConverter for Solr
tbd

* 

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
