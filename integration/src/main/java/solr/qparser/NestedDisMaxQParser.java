package solr.qparser;

import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SyntaxError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NestedDisMaxQParser extends QParser {

    // TODO: all clauses should only be accepted as local params

    private static final String SUB_QUERY_PARAM_KEY = "queries";
    private static final String TIE_PARAM_KEY = "tie";
    private static final float TIE_DEFAULT = 1.0f;

    private final SolrParams mergedParams;

    public NestedDisMaxQParser(
            final String qstr, final SolrParams localParams, final SolrParams params, final SolrQueryRequest req) {

        super(qstr, localParams, params, req);
        mergedParams = SolrParams.wrapDefaults(localParams, params);
    }

    @Override
    public Query parse() throws SyntaxError {
        final float tie = mergedParams.getFloat(TIE_PARAM_KEY, TIE_DEFAULT);
        final List<Query> nestedQueries = extractNestedQueries();

        return new DisjunctionMaxQuery(nestedQueries, tie);
    }

    private List<Query> extractNestedQueries() throws SyntaxError {
        final List<Query> nestedQueries = new ArrayList<>();

        for (final String nestedQueryDefinition : getNestedQueryDefinitions()) {
            final QParser queryParser = super.subQuery(nestedQueryDefinition, null);
            nestedQueries.add(queryParser.parse());
        }

        return nestedQueries;
    }

    private List<String> getNestedQueryDefinitions() {
        final String[] nestedQueryDefinitions = mergedParams.getParams(SUB_QUERY_PARAM_KEY);
        return nestedQueryDefinitions == null ? Collections.emptyList() : Arrays.asList(nestedQueryDefinitions);
    }

}
