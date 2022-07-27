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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
public class SolrTestJsonRequest {

    private final String handler;
    private final Map<String, List<String>> params;

    private final Map<String, Object> query;
    private final SolrClient solrClient;

    @Builder.Default private final String collection = "collection1";

    private final String user;
    private final String password;

    public SolrTestResult applyRequest() throws Exception {
        final JsonQueryRequest request = createRequest();

        final QueryResponse response = request.process(solrClient, collection);
        final SolrDocumentList solrDocuments = response.getResults();

        return extractResults(solrDocuments);
    }

    private JsonQueryRequest createRequest() {
        final SolrParams solrParams = createSolrParams();

        final JsonQueryRequest request = new JsonQueryRequest(solrParams);
        request.setQuery(query);
        addCredentialsIfProvided(request);

        return request;
    }

    private void addCredentialsIfProvided(final JsonQueryRequest request) {
        if (user != null && password != null) {
            request.setBasicAuthCredentials(user, password);
        }
    }

    private SolrParams createSolrParams() {
        final ModifiableSolrParams solrParams = new ModifiableSolrParams();
        params.forEach((key, values) -> solrParams.add(key, values.toArray(new String[]{})));
        return solrParams;
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
