package querqy.converter.solr.map.builder;

import lombok.RequiredArgsConstructor;
import querqy.converter.generic.builder.PhraseQueryBuilder;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor(staticName = "create")
public class SolrMapPhraseQueryBuilder implements PhraseQueryBuilder<Map<String, Object>> {

    @Override
    public Map<String, Object> build(final String field, final List<String> terms, final int slop, final float boost) {
        final StringBuilder query = new StringBuilder("\"");
        terms.stream().map(SolrMapPhraseQueryBuilder::escapeTerm).forEach(t -> {
            if (query.length() > 1) query.append(' ');
            query.append(t);
        });
        query.append("\"");
        if (slop > 0) {
            query.append("~").append(slop);
        }
        if (boost != 1.0f) {
            query.append("^").append(boost);
        }

        return Map.of("lucene", Map.of(
                "df", field,
                "query", query.toString()
        ));
    }

    private static String escapeTerm(final String term) {
        return term.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
