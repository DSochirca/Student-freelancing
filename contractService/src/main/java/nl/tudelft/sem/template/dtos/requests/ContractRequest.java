package nl.tudelft.sem.template.dtos.requests;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.entities.Contract;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ContractRequest {
    @NotBlank(message = "Company is mandatory.")
    String companyId;
    @NotBlank(message = "Student is mandatory.")
    String studentId;
    @NotNull(message = "Please specify the hours per week.")
    Double hoursPerWeek;
    @NotNull(message = "Please specify the total hours.")
    Double totalHours;
    @NotNull(message = "Please specify the price per hour.")
    Double pricePerHour;

    public Contract toContract() {
        return new Contract(null, companyId, studentId, null,
                null, hoursPerWeek, totalHours, pricePerHour, null, null);
    }
}
