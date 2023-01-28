package nl.tudelft.sem.template.exceptions;

public class ContractNotFoundException extends Exception {
    public static final long serialVersionUID = 1234567;

    public ContractNotFoundException(Long id) {
        super("Could not find a contract with id = " + id);
    }

    public ContractNotFoundException(String companyId, String studentId) {
        super("Could not find a contract between company " + companyId
                + " and student " + studentId);
    }

}
