package querqy.converter.map;

import lombok.Builder;
import querqy.QueryConfig;
import querqy.model.AbstractNodeVisitor;
import querqy.model.BooleanClause;
import querqy.model.BooleanQuery;
import querqy.model.Clause;
import querqy.model.DisjunctionMaxClause;
import querqy.model.DisjunctionMaxQuery;
import querqy.model.MatchAllQuery;
import querqy.model.Node;
import querqy.model.Query;
import querqy.model.RawQuery;
import querqy.model.Term;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
public class NodeMapConverter extends AbstractNodeVisitor<Map<String, Object>> {

    private final QueryConfig queryConfig;
    private final Node node;
    private final boolean parseAsUserQuery;

    public Map<String, Object> convert() {
        return node.accept(this);
    }

    @Override
    public Map<String, Object> visit(final BooleanQuery booleanQuery) {
        if (booleanQuery instanceof Query) {
            return visit((Query) booleanQuery);

        } else {
            final Map<String, Object> boolNode = convertBooleanQueryToMap(booleanQuery);

            final int numberOfSubClauses = booleanQuery.getClauses().size();
            if (numberOfSubClauses > 1) {
                boolNode.put("boost", (float) 1 / (float) numberOfSubClauses);
            }

            return Map.of(queryConfig.getBoolNodeName(), boolNode);
        }
    }

    @Override
    public Map<String, Object> visit(final Query query) {
        final Map<String, Object> boolNode = convertBooleanQueryToMap(query);

        if (parseAsUserQuery && queryConfig.hasMinimumShouldMatch()) {
            boolNode.put("mm", queryConfig.getMinimumShouldMatch());
        }

        return Map.of(queryConfig.getBoolNodeName(), boolNode);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertBooleanQueryToMap(final BooleanQuery booleanQuery) {
        final Map<String, Object> boolNode = new HashMap<>(2);

        for (final BooleanClause clause : booleanQuery.getClauses()) {
            List<Map<String, Object>> clauses = (List<Map<String, Object>>) boolNode.computeIfAbsent(
                    getPropertyNameForOccur(clause.getOccur()),
                    key -> new ArrayList<>());

            clauses.add(clause.accept(this));
        }

        return boolNode;
    }

    private String getPropertyNameForOccur(Clause.Occur occur) {
        if (Clause.Occur.SHOULD.equals(occur)) {
            return "should";

        } else if (Clause.Occur.MUST.equals(occur)) {
            return "must";

        } else {
            return "must_not";
        }
    }

    @Override
    public Map<String, Object> visit(final MatchAllQuery query) {
        // TODO
        throw new UnsupportedOperationException("Not implemented so far");
    }

    @Override
    public Map<String, Object> visit(final DisjunctionMaxQuery disMaxQuery) {
        final List<Map<String, Object>> convertedClauses = convertDisMaxClauses(disMaxQuery);
        final Map<String, Object> disMaxNode = createDisMaxNode(convertedClauses);
        return Map.of(queryConfig.getDisMaxNodeName(), disMaxNode);
    }

    private List<Map<String, Object>> convertDisMaxClauses(final DisjunctionMaxQuery disMaxQuery) {
        return disMaxQuery.getClauses().stream()
                .map(this::convertDisMaxClause)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> convertDisMaxClause(final DisjunctionMaxClause clause) {
        if (clause instanceof Term) {
            return convertTerm((Term) clause);

        } else {
            return List.of(clause.accept(this));
        }
    }

    private List<Map<String, Object>> convertTerm(final Term term) {
        if (term.getField() == null) {
            return TermMapConverter.builder()
                    .queryConfig(queryConfig)
                    .value(term.toString())
                    .build()
                    .createTermQueries();

        } else {
            throw new IllegalArgumentException("Not implemented so far");
        }
    }

    private Map<String, Object> createDisMaxNode(final List<Map<String, Object>> convertedClauses) {
        if (queryConfig.hasTie()) {
            return Map.of("queries", convertedClauses, "tie", queryConfig.getTie());

        } else {
            return Map.of("queries", convertedClauses);
        }
    }

    @Override
    public Map<String, Object> visit(final RawQuery rawQuery) {
        // TODO
        throw new UnsupportedOperationException("Not supported");

    }

    @Override
    public Map<String, Object> visit(final Term term) {
        throw new UnsupportedOperationException("Not supported");
    }


}
