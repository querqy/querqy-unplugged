package solr.qparser;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;

public class ConstantScoreQParserPlugin extends QParserPlugin {

    @Override
    public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
        return new ConstantScoreQParser(qstr, localParams, params, req);
    }
}
