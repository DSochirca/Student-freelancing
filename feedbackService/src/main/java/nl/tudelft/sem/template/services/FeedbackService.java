package nl.tudelft.sem.template.services;

import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.template.domain.Feedback;
import nl.tudelft.sem.template.domain.dtos.requests.FeedbackRequest;
import nl.tudelft.sem.template.domain.dtos.responses.ContractResponse;
import nl.tudelft.sem.template.domain.dtos.responses.FeedbackResponse;
import nl.tudelft.sem.template.exceptions.InvalidRoleException;
import nl.tudelft.sem.template.exceptions.NoExistingContractException;
import nl.tudelft.sem.template.exceptions.UserServiceUnavailableException;
import nl.tudelft.sem.template.repositories.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@Service
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class FeedbackService {
    @Autowired
    private transient FeedbackRepository feedbackRepository;
    @Autowired
    private transient FeedbackServiceHelper feedbackServiceHelper;

    /**
     * Service, which gets a feedback by given id.
     *
     * @param id - the id of the desired feedback.
     * @return - a FeedbackResponse, if the feedback is found,
     *     error if no such feedback exists.
     */
    public FeedbackResponse getById(Long id) {
        Optional<Feedback> res = feedbackRepository.findById(id);

        feedbackServiceHelper.verifyGetByIdResponse(id, res);

        return res.get().to();
    }

    /**
     * Creates a feedback for a contract.
     *
     * @param feedbackRequest the feedback request
     * @param userName        the user name
     * @param userRole        the user role
     * @return the feedback response
     */
    public Pair<FeedbackResponse, Long> create(FeedbackRequest feedbackRequest, String userName,
                                               String userRole) {
        if (feedbackRequest.getFrom() == null) {
            feedbackRequest.setFrom(userName);
        }

        try {
            feedbackServiceHelper.handleCreate(feedbackRequest, userName);

            String contractUrl = feedbackServiceHelper.getContractUrl(feedbackRequest, userRole);

            // Check if there exists a contract between the two parties.
            checkExistingContract(feedbackRequest, contractUrl);

        } catch (RestClientException e) {
            throw new UserServiceUnavailableException(e.getMessage());
        } catch (IllegalArgumentException e) { // UserRole.valueOf(userRole) can throw this
            throw new InvalidRoleException("Role " + userRole + " is invalid.");
        }

        Feedback feedback = Feedback.from(feedbackRequest);
        Feedback res = feedbackRepository.save(feedback);

        return Pair.of(res.to(), res.getId());
    }

    private void checkExistingContract(FeedbackRequest feedbackRequest, String contractUrl) {
        try {

            ContractResponse contract =
                feedbackServiceHelper.getContract(feedbackRequest, contractUrl);

            feedbackServiceHelper.verifyContractCreationStatus(contract);

            // Should not be able to give feedback more than once
            List<Feedback> existingFeedbacks =
                feedbackRepository.hasReviewedBefore(feedbackRequest.getFrom(),
                    feedbackRequest.getTo(), feedbackRequest.getContractId());

            feedbackServiceHelper.checkForExistingContractFeedbackOnCreation(existingFeedbacks);
        } catch (HttpClientErrorException e) {
            String msg = "No contract found between " + feedbackRequest.getFrom() + " and "
                + feedbackRequest.getTo();

            throw new NoExistingContractException(msg);
        }
    }

    public Double getAverageRatingByUser(String userName) {
        return feedbackRepository.getAverageRatingByUser(userName);
    }
}
