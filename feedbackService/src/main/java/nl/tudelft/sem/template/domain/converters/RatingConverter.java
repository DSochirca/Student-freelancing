package nl.tudelft.sem.template.domain.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import nl.tudelft.sem.template.domain.Rating;

@Converter
public class RatingConverter implements AttributeConverter<Rating, Integer> {
    @Override
    public Integer convertToDatabaseColumn(Rating attribute) {
        return attribute.getStars();
    }

    @Override
    public Rating convertToEntityAttribute(Integer dbData) {
        return new Rating(dbData);
    }
}
