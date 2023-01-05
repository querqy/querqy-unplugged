package solr.request;

import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.request.RequestWriter;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.Utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class JsonMapQueryRequest extends QueryRequest {
    private final Map<String, Object> request;

    public JsonMapQueryRequest(final Map<String, Object> request) {
        this(request, new ModifiableSolrParams());
    }

    public JsonMapQueryRequest(final Map<String, Object> request, final SolrParams params) {
        super(params, METHOD.POST);
        this.request = request;
    }

    public RequestWriter.ContentWriter getContentWriter(String expectedType) {
        return new RequestWriter.ContentWriter() {
            @Override
            public void write(OutputStream os) throws IOException {
                Utils.writeJson(request, os, true);
            }

            @Override
            public String getContentType() {
                return ClientUtils.TEXT_JSON;
            }
        };
    }

}
