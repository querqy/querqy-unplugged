package querqy.converter.solr.map;


import lombok.RequiredArgsConstructor;
import querqy.model.RawQuery;
import querqy.model.StringRawQuery;

@RequiredArgsConstructor(staticName = "of")
public class RawQueryConverter {

    private final RawQuery rawQuery;

    public String convert() {
        if (rawQuery instanceof StringRawQuery) {
            return ((StringRawQuery) rawQuery).getQueryString();

        } else {
            throw new UnsupportedOperationException("Conversion of RawQuery is only supported for type StringRawQuery");
        }
    }
}
