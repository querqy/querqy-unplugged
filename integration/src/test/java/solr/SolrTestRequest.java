package solr;

import lombok.Builder;
import lombok.Singular;
import org.apache.lucene.document.StoredField;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import solr.request.JsonMapQueryRequest;

import java.util.HashMap;
import java.util.Map;

@Builder(toBuilder = true)
public class SolrTestRequest {

    private final SolrClient solrClient;

    private final Map<String, Object> query;
    @Singular private final Map<String, String> params;

    @Builder.Default private final String collection = "collection1";
    @Builder.Default private final String handler = "/select";
    private final String user;
    private final String password;

    public SolrTestResult applyRequest() throws Exception {
        final QueryRequest request = createRequest();
        addCredentialsIfProvided(request);

        final QueryResponse response = request.process(solrClient, collection);
        final SolrDocumentList solrDocuments = response.getResults();

        return extractResults(solrDocuments);
    }

    private QueryRequest createRequest() {
        final SolrParams solrParams = createSolrParams();

        if (query == null || query.isEmpty()) {
            return new QueryRequest(solrParams);

        } else {
            return new JsonMapQueryRequest(query, solrParams);
        }
    }

    private ModifiableSolrParams createSolrParams() {
        final ModifiableSolrParams solrParams = new ModifiableSolrParams();
        params.forEach(solrParams::add);
        return solrParams;
    }

    private void addCredentialsIfProvided(final QueryRequest request) {
        if (user != null && password != null) {
            request.setBasicAuthCredentials(user, password);
        }
    }

    private SolrTestResult extractResults(final SolrDocumentList solrDocuments) {
        final SolrTestResult docs = new SolrTestResult();
        docs.setNumFound(solrDocuments.getNumFound());

        solrDocuments.stream()
                .map(this::convertSolrDocToMap)
                .forEach(docs::add);
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

//    @SuppressWarnings({"unused", "FieldMayBeFinal"})
//    public static class SolrTestJsonRequestBuilder {
//        private Map<String, List<String>> params = new HashMap<>();
//
//        public SolrTestJsonRequest.SolrTestJsonRequestBuilder param(String paramKey, String paramValue) {
//            params.computeIfAbsent(paramKey, key -> new ArrayList<>())
//                    .add(paramValue);
//
//            return this;
//        }
//    }
}
