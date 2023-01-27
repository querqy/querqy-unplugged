package querqy.converter.generic;

import lombok.Builder;
import lombok.NonNull;
import querqy.QueryConfig;
import querqy.converter.generic.builder.BooleanQueryBuilder;
import querqy.converter.generic.builder.DismaxQueryBuilder;
import querqy.converter.generic.builder.MatchAllQueryBuilder;
import querqy.converter.generic.builder.RawQueryBuilder;
import querqy.converter.generic.model.BooleanQueryDefinition;
import querqy.converter.generic.model.DismaxQueryDefinition;
import querqy.model.AbstractNodeVisitor;
import querqy.model.BooleanClause;
import querqy.model.BooleanQuery;
import querqy.model.DisjunctionMaxClause;
import querqy.model.DisjunctionMaxQuery;
import querqy.model.MatchAllQuery;
import querqy.model.QuerqyQuery;
import querqy.model.Query;
import querqy.model.RawQuery;
import querqy.model.StringRawQuery;
import querqy.model.Term;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Builder
public class GenericQuerqyQueryConverter<T> extends AbstractNodeVisitor<T> {

    @NonNull private final QueryConfig queryConfig;

    @NonNull private final BooleanQueryBuilder<T> booleanQueryBuilder;
    @NonNull private final DismaxQueryBuilder<T> dismaxQueryBuilder;

    @NonNull private final GenericTermConverter<T> genericTermConverter;

    @NonNull private final MatchAllQueryBuilder<T> matchAllQueryBuilder;
    @NonNull private final RawQueryBuilder<T> rawQueryBuilder;

    public T convert(final QuerqyQuery<?> querqyQuery) {
        return querqyQuery.accept(this);
    }

    @Override
    public T visit(final Query query) {
        return visit((BooleanQuery) query);
    }

    @Override
    public T visit(final BooleanQuery booleanQuery) {
        final BooleanQueryDefinition.BooleanQueryDefinitionBuilder<T> builder = BooleanQueryDefinition.<T>builder();

        for (final BooleanClause clause : booleanQuery.getClauses()) {
            switch (clause.getOccur()) {
                case SHOULD:
                    builder.shouldClause(clause.accept(this)); break;

                case MUST:
                    builder.mustClause(clause.accept(this)); break;

                case MUST_NOT:
                    builder.mustNotClause(clause.accept(this)); break;

                default:
                    throw new IllegalArgumentException("GenericQuerqyQueryConverter does not support Occur type " + clause.getOccur());
            }
        }

        if (builder.numberOfShouldClauses() == 0) {
            builder.boost(builder.numberOfMustClauses() > 0 ? (float) 1 / (float) builder.numberOfMustClauses() : 0.0f);

        } else {
            builder.boost(1.0f);
        }


        if (booleanQuery instanceof Query) {
            queryConfig.getMinimumShouldMatch().ifPresent(builder::minimumShouldMatch);
        }

        return booleanQueryBuilder.build(builder.build());
    }

    @Override
    public T visit(final DisjunctionMaxQuery disMaxQuery) {
        final List<T> dismaxClauses = convertDismaxClauses(disMaxQuery);

        final DismaxQueryDefinition.DismaxQueryDefinitionBuilder<T> definitionBuilder = DismaxQueryDefinition.<T>builder()
                .dismaxClauses(dismaxClauses);

        queryConfig.getTie().ifPresent(definitionBuilder::tie);

        return dismaxQueryBuilder.build(definitionBuilder.build());
    }

    private List<T> convertDismaxClauses(final DisjunctionMaxQuery dismaxQuery) {
        return dismaxQuery.getClauses().stream()
                .map(this::convertDismaxClause)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<T> convertDismaxClause(final DisjunctionMaxClause clause) {
        if (clause instanceof Term) {
            return convertTerm((Term) clause);

        } else {
            return List.of(clause.accept(this));
        }
    }

    private List<T> convertTerm(final Term term) {
        return genericTermConverter.convert(term);
    }

    @Override
    public T visit(final Term term) {
        throw new UnsupportedOperationException("Visiting a single term is not supported for " + this.getClass().getName());
    }

    @Override
    public T visit(final MatchAllQuery query) {
        return matchAllQueryBuilder.build();
    }

    @Override
    public T visit(final RawQuery rawQuery) {
        if (rawQuery instanceof StringRawQuery) {
            final StringRawQuery stringRawQuery = (StringRawQuery) rawQuery;
            return rawQueryBuilder.buildFromString(stringRawQuery.getQueryString());

        } else {
            throw new IllegalArgumentException("GenericQuerqyQueryConverter currently only supports StringRawQuery");
        }
    }

}
