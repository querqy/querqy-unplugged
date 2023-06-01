package querqy.converter.solr.map.builder;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor(staticName = "create")
public class SolrMapQueryReferenceBuilder {

    private static final String REFERENCE_PREFIX = "_querqy_ref";

    private final Map<String, Object> references = new HashMap<>();

    private int referenceCount = 0;

    public String createReferenceForQuery(final Object query) {
        final String reference = createReference();
        references.put(reference, query);
        return reference;
    }

    public Map<String, Object> getReferences() {
        return references;
    }

    private String createReference() {
        return REFERENCE_PREFIX + referenceCount++;
    }

}
