package solr.qparser;

import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SyntaxError;

public class ConstantScoreQParser extends QParser {

    // TODO: all clauses should only be accepted as local params

    private static final String SUB_QUERY_PARAM_KEY = "filter";
    private static final String BOOST_PARAM_KEY = "boost";

    public ConstantScoreQParser(
            final String qstr, final SolrParams localParams, final SolrParams params, final SolrQueryRequest req) {
        super(qstr, localParams, params, req);
    }

    @Override
    public Query parse() throws SyntaxError {
        final Query subQuery = parseSubQuery();
        final ConstantScoreQuery constantScoreQuery = new ConstantScoreQuery(subQuery);

        return potentiallyWrapByBoostQuery(constantScoreQuery);
    }

    private Query parseSubQuery() throws SyntaxError {
        final String subQueryDefinition = getLocalParams().get(SUB_QUERY_PARAM_KEY);
        final QParser subQueryParser = super.subQuery(subQueryDefinition, null);
        return subQueryParser.parse();
    }

    private Query potentiallyWrapByBoostQuery(final Query query) {
        final Float boost = getLocalParams().getFloat(BOOST_PARAM_KEY);

        if (boost == null) {
            return query;

        } else {
            return new BoostQuery(query, boost);
        }
    }

}
