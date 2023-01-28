package nl.tudelft.sem.template.services;

import java.util.Objects;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.entities.dtos.AverageRatingResponseWrapper;
import nl.tudelft.sem.template.entities.dtos.ContractDto;
import nl.tudelft.sem.template.entities.dtos.UserResponseWrapper;
import nl.tudelft.sem.template.exceptions.ContractCreationException;
import nl.tudelft.sem.template.exceptions.UpstreamServiceException;
import nl.tudelft.sem.template.exceptions.UserDoesNotExistException;
import nl.tudelft.sem.template.exceptions.UserServiceUnvanvailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@NoArgsConstructor
public class Utility {

    @Autowired
    private transient RestTemplate restTemplate;

    /**
     * Checks to see if the given user exists.
     *
     * @param userId       The user id.
     */
    public void userExists(String userId)
            throws UserDoesNotExistException, UserServiceUnvanvailableException {
        try {
            var url = "http://users-service/" + userId;
            var res = restTemplate.getForObject(url, UserResponseWrapper.class);

            if (res == null) {
                throw new UserServiceUnvanvailableException();
            }

            if (res.getData() == null) {
                throw new UserDoesNotExistException(res.getErrorMessage());
            }
        } catch (RestClientException e) {
            throw new UserServiceUnvanvailableException(e.getMessage());
        }
    }

    /**
     * Creates a contract between 2 parties by making a request to the contract microservice.
     *
     * @param companyId    The company's id.
     * @param studentId    The student's id.
     * @param hoursPerWeek How many hours per week.
     * @param totalHours   The amount of total hours.
     * @param pricePerHour The price per hour.
     * @return The created contract entity.
     * @throws ContractCreationException If the contract parameters were invalid
     *                                or if the request or if the contract service was unavailable.
     */
    public ContractDto createContract(String companyId, String studentId, Double hoursPerWeek,
                                      Double totalHours, Double pricePerHour)
            throws ContractCreationException {

        ContractDto sentDto
                = new ContractDto(companyId, studentId, hoursPerWeek, totalHours, pricePerHour);

        try {
            System.out.println("Sending contract to contract microservice");
            String url = "http://contract-service/";

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-user-role", "INTERNAL_SERVICE");

            HttpEntity<ContractDto> requestEntity = new HttpEntity<>(sentDto, headers);
            return restTemplate
                    .postForObject(url, requestEntity, ContractDto.class);

        } catch (RestClientException e) {
            // Error message from the contract microservice:
            throw new ContractCreationException(e.getMessage());
        }
    }

    /**
     * Returns the average rating of a user by contacting the user feedback service.
     *
     * @param username username of the user.
     * @return average rating of the user.
     * @throws UpstreamServiceException Thrown when the user feedback service is not available.
     */
    public double getAverageRating(String username) throws UpstreamServiceException {
        String feedbackServiceUrl = "http://feedback-service/user/" + username;
        try {
            AverageRatingResponseWrapper response = restTemplate.getForObject(
                    feedbackServiceUrl, AverageRatingResponseWrapper.class
            );
            System.out.println(Objects.requireNonNull(response).getData().getAverageRating());
            Objects.requireNonNull(response);
            Objects.requireNonNull(response.getData());
            return response.getData().getAverageRating();
        } catch (Exception exception) {
            throw new UpstreamServiceException(
                    "Unable to get an average rating for " + username + " from feedback service.",
                    exception
            );
        }
    }
}
