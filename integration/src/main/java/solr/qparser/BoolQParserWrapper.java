package solr.qparser;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.*;
import org.apache.solr.util.SolrPluginUtils;

public class BoolQParserWrapper extends QParser {

    // TODO: all clauses should only be accepted as local params
    private static final String BOOST_PARAM_KEY = "boost";

    private final QParser parser;
    private final SolrParams mergedParams;

    public BoolQParserWrapper(final QParser parser, String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
        super(qstr, localParams, params, req);

        this.parser = parser;
        this.mergedParams = SolrParams.wrapDefaults(localParams, params);
    }

    @Override
    public Query parse() throws SyntaxError {
        final Query query = parser.parse();

        if (query instanceof BooleanQuery) {
            final String minShouldMatch = parseMinShouldMatch();
            final Query queryWithMinShouldMatch = SolrPluginUtils.setMinShouldMatch(
                    (BooleanQuery) query, minShouldMatch);

            return potentiallyWrapByBoostQuery(queryWithMinShouldMatch);

        } else {
            // TODO: some kind of exception handling? this should never happen
            return query;
        }
    }

    private String parseMinShouldMatch() {
        return DisMaxQParser.parseMinShouldMatch(super.req.getSchema(), mergedParams);
    }

    private Query potentiallyWrapByBoostQuery(final Query query) {
        final Float boost = mergedParams.getFloat(BOOST_PARAM_KEY);

        if (boost == null) {
            return query;

        } else {
            return new BoostQuery(query, boost);
        }
    }
}
