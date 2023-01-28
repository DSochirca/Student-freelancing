package nl.tudelft.sem.template.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class StringListConverterTest {

    static StringListConverter converter = new StringListConverter();

    @Test
    void convertToDatabaseColumn() {
        List<String> list = Arrays.asList("Hello", "World", "!");
        assertEquals("Hello;World;!", converter.convertToDatabaseColumn(list));
    }

    @Test
    void nullArrayTest() {
        assertEquals("", converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute() {
        String s = "Hello;World;!";
        assertEquals(Arrays.asList("Hello", "World", "!"), converter.convertToEntityAttribute(s));
    }
}