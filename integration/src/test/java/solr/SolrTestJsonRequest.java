package solr;

import lombok.Builder;
import org.apache.lucene.document.StoredField;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.json.JsonQueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.BasicResultContext;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.util.TestHarness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.solr.SolrTestCaseJ4.req;

@Builder
public class SolrTestJsonRequest {

    private final String handler;
    private final Map<String, List<String>> params;

    private final Map query;
    private final SolrClient solrClient;

    public SolrTestResult applyRequest() throws Exception {
        final SolrParams solrParams = createSolrParams();
        final JsonQueryRequest request = new JsonQueryRequest(solrParams);
        request.setQuery(query);

        final QueryResponse response = request.process(solrClient, "collection1");
        final SolrDocumentList solrDocuments = response.getResults();

        System.out.println(response);


//        final SolrQueryRequest req = req(solrParams);
//
//        final SolrQueryResponse resp = testHarness.queryAndResponse(handler, req);
//        final SolrTestResult docs = extractResults(resp);
//
//        req.close();
//
//        return docs;
        return null;
    }

    private SolrParams createSolrParams() {
        final ModifiableSolrParams solrParams = new ModifiableSolrParams();
        params.forEach((key, values) -> solrParams.add(key, values.toArray(new String[]{})));
        return solrParams;
    }

    private SolrTestResult extractResults(final SolrQueryResponse resp) {
        final SolrTestResult docs = new SolrTestResult();
        ((BasicResultContext) resp.getResponse())
                .getProcessedDocuments()
                .forEachRemaining(doc -> docs.add(convertSolrDocToMap(doc)));
        return docs;
    }

    private Map<String, Object> convertSolrDocToMap(final SolrDocument doc) {
        final Map<String, Object> convertedDoc = new HashMap<>();

        for (final String fieldName : doc.getFieldNames()) {
            final Object fieldValue = extractValueFromSolrDoc(fieldName, doc);
            convertedDoc.put(fieldName, fieldValue);
        }

        return convertedDoc;
    }

    private Object extractValueFromSolrDoc(final String fieldName, final SolrDocument doc) {
            final Object value = doc.getFirstValue(fieldName);

            if (value instanceof StoredField) {
                return ((StoredField) value).stringValue();

            } else {
                return value;
            }

    }

    //@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection", "FieldMayBeFinal"})
    public static class SolrTestJsonRequestBuilder {
        private Map<String, List<String>> params = new HashMap<>();

        public SolrTestJsonRequest.SolrTestJsonRequestBuilder param(String paramKey, String paramValue) {
            params.computeIfAbsent(paramKey, key -> new ArrayList<>())
                    .add(paramValue);

            return this;
        }
    }
}
