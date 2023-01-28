package nl.tudelft.sem.template.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.enums.Status;

@Data
@NoArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
public class StudentOffer extends Offer {

    private double pricePerHour;
    private String studentId;

    @JsonIgnore
    @OneToMany(mappedBy = "studentOffer")
    private List<TargetedCompanyOffer> targetedCompanyOffers;

    /**
     * Constructor for the StudentOffer class.
     *
     * @param title        String with title of the offer.
     * @param description  String with description of the offer.
     * @param hoursPerWeek Double indicating hours per week of the offer.
     * @param totalHours   Double indicating total hours this offer entails.
     * @param expertise    List of String with the expertise associated with this offer.
     * @param status       Status of type enum indicating accepted/declined/pending/disabled.
     * @param pricePerHour Double with the price per hour of this offer.
     * @param studentId    String of the student ID.
     */
    public StudentOffer(String title, String description, double hoursPerWeek,
                        double totalHours, List<String> expertise, Status status,
                        double pricePerHour, String studentId) {
        super(title, description, hoursPerWeek, totalHours, expertise, status);
        this.pricePerHour = pricePerHour;
        this.studentId = studentId;
    }

    @Override
    public String getCreatorUsername() {
        return studentId;
    }
}
