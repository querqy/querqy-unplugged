package querqy.converter.solr.map.builder;

import lombok.RequiredArgsConstructor;
import querqy.converter.generic.builder.MatchAllQueryBuilder;

import java.util.Map;

@RequiredArgsConstructor(staticName = "create")
public class SolrMapMatchAllQueryBuilder implements MatchAllQueryBuilder<Map<String, Object>> {

    @Override
    public Map<String, Object> build() {
        return Map.of("lucene", Map.of("query", "*:*"));
    }
}
