package nl.tudelft.sem.template.services;

import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.template.domain.Feedback;
import nl.tudelft.sem.template.domain.dtos.enums.Status;
import nl.tudelft.sem.template.domain.dtos.enums.UserRole;
import nl.tudelft.sem.template.domain.dtos.requests.FeedbackRequest;
import nl.tudelft.sem.template.domain.dtos.responses.ContractResponse;
import nl.tudelft.sem.template.domain.dtos.responses.UserRoleResponseWrapper;
import nl.tudelft.sem.template.exceptions.ContractNotExpiredException;
import nl.tudelft.sem.template.exceptions.FeedbackAlreadyExistsException;
import nl.tudelft.sem.template.exceptions.FeedbackNotFoundException;
import nl.tudelft.sem.template.exceptions.InvalidFeedbackDetailsException;
import nl.tudelft.sem.template.exceptions.InvalidUserException;
import nl.tudelft.sem.template.exceptions.UserServiceUnavailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class FeedbackServiceHelper {
    @Autowired
    private transient RestTemplate restTemplate;

    /**
     * Makes sure that a user is not reviewing himself.
     *
     * @param feedbackRequest The feedback request
     * @param userName The username
     */
    public void handleCreate(FeedbackRequest feedbackRequest, String userName) {
        if (userName.equals(feedbackRequest.getTo()) || feedbackRequest.getTo()
            .equals(feedbackRequest.getFrom())) {
            throw new InvalidFeedbackDetailsException("Cant add a review of yourself");
        }
    }

    /**
     * Validates the target role.
     *
     * @param targetRole The target role
     * @param recipientUser The recipient user
     */
    private void validateRole(UserRole targetRole, UserRoleResponseWrapper recipientUser) {
        if (recipientUser == null) {
            throw new UserServiceUnavailableException();
        }

        if (recipientUser.getErrorMessage() != null) {
            throw new InvalidUserException("Recipient does not exist.");
        }

        if (UserRole.valueOf(recipientUser.getData().getRole()) != targetRole) {
            throw new InvalidUserException("The recipient has the same role as the author.");
        }
    }

    /**
     * Ensures the response is not empty.
     *
     * @param id The feedback id
     * @param res The response
     */
    public void verifyGetByIdResponse(Long id, Optional<Feedback> res) {
        if (res.isEmpty()) {
            throw new FeedbackNotFoundException("Feedback with id " + id + " does not exist.");
        }
    }

    /**
     * Ensures that the contract status is not active on creation.
     *
     * @param contract The contract to be created
     */
    public void verifyContractCreationStatus(ContractResponse contract) {
        if (Status.valueOf(contract.getStatus()) == Status.ACTIVE) {
            String msg = "Can't leave feedback while contract is still active.";
            throw new ContractNotExpiredException(msg);
        }
    }

    /**
     * Ensures that there's no existing feedback.
     *
     * @param existingFeedbacks The existing feedback
     */
    public void checkForExistingContractFeedbackOnCreation(List<Feedback> existingFeedbacks) {
        if (!existingFeedbacks.isEmpty()) {
            String msg = "You have already given feedback for this contract";
            throw new FeedbackAlreadyExistsException(msg);
        }
    }

    /**
     * Generates the get contract url.
     *
     * @param feedbackRequest The feedback request
     * @param userRole The user role
     * @return The contract url
     */
    public String getContractUrl(FeedbackRequest feedbackRequest, String userRole) {
        // Check if student/company exist
        // Target role should be the opposite of the user role
        UserRole targetRole = getRecipientRole(feedbackRequest, userRole);

        // Author and recipient must have a contract to leave feedback.
        // If the target role is a student then the author is a company and vice-versa.
        String companyName;
        String studentName;
        if (targetRole == UserRole.STUDENT) {
            companyName = feedbackRequest.getFrom();
            studentName = feedbackRequest.getTo();
        } else {
            companyName = feedbackRequest.getTo();
            studentName = feedbackRequest.getFrom();
        }

        return "http://contract-service/" + companyName + "/" + studentName + "/mostRecent";
    }

    /**
     * Gets the recipient role from the user service.
     *
     * @param feedbackRequest The feedback request
     * @param userRole The user role
     * @return The recipient role
     */
    private UserRole getRecipientRole(FeedbackRequest feedbackRequest, String userRole) {
        String baseUserUrl = "http://users-service/";
        String urlRecipient = baseUserUrl + feedbackRequest.getTo();

        // Get the target role
        UserRole targetRole =
            UserRole.valueOf(userRole) == UserRole.STUDENT ? UserRole.COMPANY : UserRole.STUDENT;

        UserRoleResponseWrapper recipientUser =
            restTemplate.getForObject(urlRecipient, UserRoleResponseWrapper.class);

        validateRole(targetRole, recipientUser);

        return targetRole;
    }

    /**
     * Gets the contract from the contract service.
     *
     * @param feedbackRequest The feedback request
     * @param contractUrl The contract url
     * @return The Contract
     */
    public ContractResponse getContract(FeedbackRequest feedbackRequest, String contractUrl) {
        // Headers of the request:
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-user-name", feedbackRequest.getFrom());

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // getForObject doesn't support headers, use exchange instead:
        ContractResponse contract =
            restTemplate.exchange(contractUrl, HttpMethod.GET, requestEntity,
                ContractResponse.class).getBody();
        return contract;
    }
}
