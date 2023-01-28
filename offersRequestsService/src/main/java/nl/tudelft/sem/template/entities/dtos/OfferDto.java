package nl.tudelft.sem.template.entities.dtos;

import java.util.List;
import javax.persistence.Convert;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.tudelft.sem.template.converters.StringListConverter;
import nl.tudelft.sem.template.entities.NonTargetedCompanyOffer;
import nl.tudelft.sem.template.entities.StudentOffer;
import nl.tudelft.sem.template.entities.TargetedCompanyOffer;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class OfferDto {
    @NotBlank(message = "Please enter a title!")
    private String title;
    @NotBlank(message = "Please enter a description!")
    private String description;
    @NotNull(message = "Please enter the number of hours per week!")
    private double hoursPerWeek;
    @NotNull(message = "Please enter the total number of hours!")
    private double totalHours;
    @NotNull(message = "Please enter some expertise!")
    @Convert(converter = StringListConverter.class)
    private List<String> expertise;
    @Convert(converter = StringListConverter.class)
    private List<String> requirements;
    private String companyId;
    private double pricePerHour;
    private String studentId;

    /**
     * Creates a TargetedCompanyOffer out of the DTO.
     *
     * @return - a new TargetedCompanyOffer.
     */
    public TargetedCompanyOffer toTargetedCompanyOffer() {
        return new TargetedCompanyOffer(title, description, hoursPerWeek,
                totalHours, expertise, null, requirements, companyId, null);
    }

    /**
     * Creates a NonTargetedCompanyOffer out of the DTO.
     *
     * @return - a new NonTargetedCompanyOffer.
     */
    public NonTargetedCompanyOffer toNonTargetedCompanyOffer() {
        return new NonTargetedCompanyOffer(title, description, hoursPerWeek,
                totalHours, expertise, null, requirements, companyId);
    }

    /**
     * Creates a StudentOffer out of the DTO.
     *
     * @return - a new StudentOffer.
     */
    public StudentOffer toStudentOffer() {
        return new StudentOffer(title, description, hoursPerWeek,
                totalHours, expertise, null, pricePerHour, studentId);
    }
}
