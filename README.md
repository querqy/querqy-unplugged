# querqy-for-service

## Overview

TBD

## Applying rewriting outside a search engine

### Rewriting in a (micro)service vs. rewriting as a plugin
* More flexibility regarding the hosting and the selection of the search engine
* More flexibility regarding the development of rewriting features (lightweight deployments, no interference with indexing)
* Less flexibility regarding features with direct Lucene access or Lucene customizations 

### Modes of interacting with search engines
| Mode                                                                                  | Solr                  | Elasticsearch / OpenSearch    
| ------------                                                                          | :-----------:         | :-----------:                   
| (1) Vanilla (only out-of-the-box functionalities)                                     | :x:                   | :white_check_mark:                      
| (2) Search API Plugin for accessing Lucene via API (e.g. enhanced Query Parsers)      | :white_check_mark:    | :white_circle:                      
| (3) Querqy API Plugin (Querqy communicates with Querqy)                               | :white_check_mark:    | :white_check_mark:                      

The main benefit of running a vanilla search engine in mode (1) is the flexibility regarding the hosting as they do not 
require any customizations and can even be hosted externally. 

Elasticsearch / OpenSearch do not require plugins for making Lucene accessible via an API (mode 2) as their Query DSL already
covers this sufficiently. 

The main difference between (2) and (3) is the flexibility of switching the search engine. A switch from Solr in mode (2)
to ES / OS in mode (1) is quite straightforward, whereas a switch from Solr in mode (3) to ES / OS requires the Querqy 
API plugin also for ES / OS. 

The easiest way to switch from a pure plugin-based rewriting to a service-based rewriting should be mode (3), as this does
not require refactorings of the Querqy pipeline.
 

### Limitations (known so far)
* The current Wordbreak rewriter requires access to a Lucene index.
* The multi-match-tie feature is currently implemented in querqy-lucene and is needed to be moved into querqy-core.
However, the current implementation produces too many clauses for the case of largely expanded queries. Therefore,
the feature might be implemented using a custom Lucene query in the future, which will make it unsuitable for options
(1) and (2). 
* Term frequency faking
* Splitting terms in Lucene analyzer chains applied inside the search engine will lead to odd scoring


### Open for discussion
* Handling negative boosts (e.g. in the context of DOWN-rules in the common rules rewriter)
* Handling of eDismax-like params (e.g. bq, bf)