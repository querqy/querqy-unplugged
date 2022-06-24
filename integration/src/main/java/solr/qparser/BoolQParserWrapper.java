package solr.qparser;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.*;
import org.apache.solr.util.SolrPluginUtils;

public class BoolQParserWrapper extends QParser {

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
            return SolrPluginUtils.setMinShouldMatch((BooleanQuery) query, minShouldMatch);

        } else {
            // TODO: some kind of exception handling? this should never happen
            return query;
        }
    }

    private String parseMinShouldMatch() {
        return DisMaxQParser.parseMinShouldMatch(super.req.getSchema(), mergedParams);
    }
}
