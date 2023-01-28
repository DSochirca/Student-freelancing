package nl.tudelft.sem.template.entities;

import java.util.List;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.converters.StringListConverter;
import nl.tudelft.sem.template.enums.Status;

@Data
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@EqualsAndHashCode
public abstract class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String title;
    private String description;
    private double hoursPerWeek;
    private double totalHours;
    @Convert(converter = StringListConverter.class)
    private List<String> expertise;
    @Enumerated(EnumType.STRING)
    private Status status;

    /**
     * Constructor for the Offer class.
     *
     * @param title String with title of the offer.
     * @param description String with description of the offer.
     * @param hoursPerWeek Double indicating hours per week of the offer.
     * @param totalHours Double indicating total hours this offer entails.
     * @param expertise List of String with the expertise associated with this offer.
     * @param status Status of type enum indicating, can be accepted/declined/pending/disabled.
     */
    public Offer(String title, String description, double hoursPerWeek,
                 double totalHours, List<String> expertise, Status status) {
        this.title = title;
        this.description = description;
        this.hoursPerWeek = hoursPerWeek;
        this.totalHours = totalHours;
        this.expertise = expertise;
        this.status = status;
    }

    /**
     * Return the company or student username that created the offer.
     *
     * @return String with the username of the company or student that created the offer.
     */
    public abstract String getCreatorUsername();
}
