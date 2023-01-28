package nl.tudelft.sem.template.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.enums.ContractStatus;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String companyId;
    private String studentId;
    private LocalDate startDate;
    private LocalDate endDate;
    private double hoursPerWeek;
    private double totalHours;
    private double pricePerHour;
    @Enumerated(EnumType.STRING)
    private ContractStatus status;

    @JsonIgnore
    @OneToMany(mappedBy = "contract")
    private List<ContractChangeProposal> proposedChanges;

}
