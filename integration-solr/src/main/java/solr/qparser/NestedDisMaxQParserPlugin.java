package solr.qparser;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;

public class NestedDisMaxQParserPlugin extends QParserPlugin {

    @Override
    public QParser createParser(String qstr, SolrParams localParams, SolrParams globalParams, SolrQueryRequest solrQueryRequest) {
        return new NestedDisMaxQParser(qstr, localParams, globalParams, solrQueryRequest);
    }
}
