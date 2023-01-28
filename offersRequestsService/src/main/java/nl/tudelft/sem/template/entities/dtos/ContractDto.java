package nl.tudelft.sem.template.entities.dtos;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
//DTO for sending info in order to create a contract, and for retrieving it
public class ContractDto {
    private Long id;
    private String companyId;
    private String studentId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double hoursPerWeek;
    private Double totalHours;
    private Double pricePerHour;
    private String status;

    /**
     * Constructs a DTO which is sent to the contract service to create the contract.
     * Other params are null and won't be considered by the contract service.
     *
     * @param companyId    The company's id.
     * @param studentId    The student's id.
     * @param hoursPerWeek How many hours per week.
     * @param totalHours   The amount of total hours.
     * @param pricePerHour The price per hour.
     */
    public ContractDto(String companyId, String studentId, Double hoursPerWeek,
                       Double totalHours, Double pricePerHour) {
        this.companyId = companyId;
        this.studentId = studentId;
        this.hoursPerWeek = hoursPerWeek;
        this.totalHours = totalHours;
        this.pricePerHour = pricePerHour;
    }
}
