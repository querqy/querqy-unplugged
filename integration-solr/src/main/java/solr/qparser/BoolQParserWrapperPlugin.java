package solr.qparser;

import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.BoolQParserPlugin;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;

public class BoolQParserWrapperPlugin extends QParserPlugin {
    @Override
    public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
        final BoolQParserPlugin boolQParserPlugin = new BoolQParserPlugin();
        final SolrParams localSolrParams = localParams == null ? new ModifiableSolrParams() : localParams;

        // Solr 9's BoolQParserPlugin calls getInt("mm") on localParams, which breaks for percentage
        // values like "100%". Strip mm here; BoolQParserWrapper applies it via setMinShouldMatch.
        final ModifiableSolrParams localParamsWithoutMm = new ModifiableSolrParams(localSolrParams);
        localParamsWithoutMm.remove("mm");

        final QParser boolQParser = boolQParserPlugin.createParser(qstr, localParamsWithoutMm, params, req);

        return new BoolQParserWrapper(boolQParser, qstr, localParams, params, req);
    }
}
