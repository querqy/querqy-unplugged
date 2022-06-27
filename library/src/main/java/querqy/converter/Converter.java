package querqy.converter;

public interface Converter<T> {

    T convert();
    String convertToJson();

}
