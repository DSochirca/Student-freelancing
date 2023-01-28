package nl.tudelft.sem.template.entities;

import java.util.List;
import javax.persistence.Convert;
import javax.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.converters.StringListConverter;
import nl.tudelft.sem.template.enums.Status;

@Data
@NoArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
public abstract class CompanyOffer extends Offer {

    @Convert(converter = StringListConverter.class)
    private List<String> requirements;
    private String companyId;

    /** Constructor for the CompanyOffer class.
     *
     * @param title String with title of the offer.
     * @param description String with description of the offer.
     * @param hoursPerWeek Double indicating hours per week of the offer.
     * @param totalHours Double indicating total hours this offer entails.
     * @param expertise List of String with the expertise associated with this offer.
     * @param status Status of type enum indicating, can be accepted/declined/pending/disabled.
     * @param requirements Requirements for this company offer of type List of String.
     * @param companyId String of the company ID.
     */
    public CompanyOffer(String title, String description, double hoursPerWeek,
                        double totalHours, List<String> expertise, Status status,
                        List<String> requirements, String companyId) {
        super(title, description, hoursPerWeek, totalHours, expertise, status);
        this.requirements = requirements;
        this.companyId = companyId;
    }


    @Override
    public String getCreatorUsername() {
        return companyId;
    }
}
