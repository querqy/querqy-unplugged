package querqy.converter;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface Converter<T> {

    T convert();
    String convertToJson();

}
