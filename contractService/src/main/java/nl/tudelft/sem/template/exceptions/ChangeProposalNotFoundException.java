package nl.tudelft.sem.template.exceptions;

public class ChangeProposalNotFoundException extends Exception {
    public static final long serialVersionUID = 1234569;

    /**
     * Throw a changeProposalNotFound exception.
     *
     * @param id The id of the proposal that wasn't found.
     */
    public ChangeProposalNotFoundException(Long id) {
        super("Could not find a change proposal with id = " + id
                + ". Possible causes could be: expired/terminated contract, "
                + "deleted proposal or invalid proposal id.");
    }

    public ChangeProposalNotFoundException(String msg) {
        super(msg);
    }
}