package querqy.rewriter.builder;

import lombok.NoArgsConstructor;
import querqy.model.Clause;
import querqy.model.DisjunctionMaxQuery;
import querqy.model.ExpandedQuery;
import querqy.model.MatchAllQuery;
import querqy.model.Query;
import querqy.model.Term;
import querqy.parser.QuerqyParser;
import querqy.rewrite.commonrules.QuerqyParserFactory;

import java.util.List;

@NoArgsConstructor(staticName = "create")
public class ExpandedQueryParser {
    public static final String MATCH_ALL_FIELD_VALUE_INPUT = "*:*";
    public static final String MATCH_ALL_WILDCARD_INPUT = "*";

    public ExpandedQuery parseQuery(final List<String> normalizedQueryTokenList, final String queryInput) {
        if (MATCH_ALL_WILDCARD_INPUT.equals(queryInput)
                || MATCH_ALL_FIELD_VALUE_INPUT.equals(queryInput)) {
            return new ExpandedQuery(new MatchAllQuery());
        }

        Query query = new Query();

        for (String normalizedToken : normalizedQueryTokenList) {
            DisjunctionMaxQuery dmq = new DisjunctionMaxQuery(query, Clause.Occur.SHOULD, false);
            dmq.addClause(new Term(dmq, normalizedToken));
            query.addClause(dmq);
        }

        // if the stopwords eliminates all terms, we add the input to the query
        if (query.getClauses().isEmpty()) {
            DisjunctionMaxQuery dmq = new DisjunctionMaxQuery(query, Clause.Occur.SHOULD, false);
            dmq.addClause(new Term(dmq, queryInput));
            query.addClause(dmq);
        }
        return new ExpandedQuery(query);
    }

    public ExpandedQuery parseQuery(final QuerqyParserFactory parserFactory, final String queryInput) {
        if (MATCH_ALL_WILDCARD_INPUT.equals(queryInput) || MATCH_ALL_FIELD_VALUE_INPUT.equals(queryInput)) {
            return new ExpandedQuery(new MatchAllQuery());

        } else {
            final QuerqyParser parser = parserFactory.createParser();

            final Query parsedQuery = parser.parse(queryInput);
            return new ExpandedQuery(parsedQuery);
        }
    }

}