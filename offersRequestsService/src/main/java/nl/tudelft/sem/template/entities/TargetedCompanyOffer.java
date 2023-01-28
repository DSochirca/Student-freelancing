package nl.tudelft.sem.template.entities;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.enums.Status;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@NoArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
public class TargetedCompanyOffer extends CompanyOffer {

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "student_offer_id", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private StudentOffer studentOffer;

    /** Constructor for the TargetedCompanyOffer class.
     *
     * @param title String with title of the offer.
     * @param description String with description of the offer.
     * @param hoursPerWeek Double indicating hours per week of the offer.
     * @param totalHours Double indicating total hours this offer entails.
     * @param expertise List of String with the expertise associated with this offer.
     * @param status Status of type enum indicating, can be accepted/declined/pending/disabled.
     * @param requirements Requirements for this company offer of type List of String.
     * @param companyId String of the company ID.
     * @param studentOffer StudentOffer
     */
    public TargetedCompanyOffer(String title, String description, double hoursPerWeek,
                                double totalHours, List<String> expertise, Status status,
                                List<String> requirements, String companyId,
                                StudentOffer studentOffer) {
        super(title, description, hoursPerWeek, totalHours, expertise,
                status, requirements, companyId);
        this.studentOffer = studentOffer;
    }
}
