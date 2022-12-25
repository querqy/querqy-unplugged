package querqy.converter;

import lombok.RequiredArgsConstructor;
import querqy.model.AbstractNodeVisitor;
import querqy.model.ExpandedQuery;
import querqy.model.QuerqyQuery;
import querqy.model.Query;
import querqy.model.Term;

import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public class TermListConverter extends AbstractNodeVisitor<Void> implements Converter<List<String>> {

    private final List<String> terms = new LinkedList<>();

    @Override
    public List<String> convert(final ExpandedQuery expandedQuery) {
        final QuerqyQuery<?> querqyQuery = expandedQuery.getUserQuery();

        if (querqyQuery instanceof Query) {
            visit((Query) querqyQuery);
        }

        return terms;
    }

    @Override
    public Void visit(final Term term) {
        terms.add(term.getValue().toString());
        return null;
    }

}
