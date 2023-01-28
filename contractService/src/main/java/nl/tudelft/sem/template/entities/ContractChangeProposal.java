package nl.tudelft.sem.template.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.time.LocalDate;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.enums.ChangeStatus;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ContractChangeProposal {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JsonProperty("contractId")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "contractId", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Contract contract;

    private String proposer;
    private String participant;

    //Changes proposed (some could be null):
    private Double hoursPerWeek;
    private Double totalHours;
    private Double pricePerHour;
    private LocalDate endDate;
    //----------------------------------

    @Enumerated(EnumType.STRING)
    private ChangeStatus status;   //the status of the change proposal(pending/rejected/accepted)

    /**
     * Constructor for a contract change proposal, without the id.
     *
     * @param contract     The contract the change is proposed to.
     * @param proposer     The proposer of the change.
     * @param participant  The user (company or student) that can accept/decline the change.
     * @param hoursPerWeek Change in hours per week (may be null).
     * @param totalHours   Change in total hours (may be null).
     * @param pricePerHour Change in price per hour (may be null).
     * @param endDate      Change of contract end date. (may be null).
     * @param status       The status of the proposal (pending/rejected/accepted).
     */
    public ContractChangeProposal(Contract contract, String proposer, String participant,
                                  Double hoursPerWeek, Double totalHours, Double pricePerHour,
                                  LocalDate endDate, ChangeStatus status) {
        this.contract = contract;
        this.proposer = proposer;
        this.participant = participant;
        this.hoursPerWeek = hoursPerWeek;
        this.totalHours = totalHours;
        this.pricePerHour = pricePerHour;
        this.endDate = endDate;
        this.status = status;
    }
}
