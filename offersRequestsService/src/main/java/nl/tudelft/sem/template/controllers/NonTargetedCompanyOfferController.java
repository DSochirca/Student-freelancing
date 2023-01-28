package nl.tudelft.sem.template.controllers;

import javax.naming.NoPermissionException;
import nl.tudelft.sem.template.entities.Application;
import nl.tudelft.sem.template.entities.NonTargetedCompanyOffer;
import nl.tudelft.sem.template.entities.Offer;
import nl.tudelft.sem.template.entities.dtos.ContractDto;
import nl.tudelft.sem.template.entities.dtos.Response;
import nl.tudelft.sem.template.exceptions.ContractCreationException;
import nl.tudelft.sem.template.exceptions.LowRatingException;
import nl.tudelft.sem.template.exceptions.UpstreamServiceException;
import nl.tudelft.sem.template.services.NonTargetedCompanyOfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/non-targeted")
public class NonTargetedCompanyOfferController {

    @Autowired
    private transient NonTargetedCompanyOfferService nonTargetedCompanyOfferService;

    private final transient String nameHeader = "x-user-name";
    private final transient String roleHeader = "x-user-role";

    /**
     * Endpoint for creating a NonTargetedCompanyOffer.
     *
     * @param nonTargetedCompanyOffer Offer we want to create
     * @param userName                Name of the user posting the offer
     * @return ResponseEntity containing either data or an errormessage.
     *          201 CREATED if request successful.
     *          401 UNAUTHORIZED if user is not logged in.
     *          403 FORBIDDEN if user not author of offer or not a company.
     *           400 BAD REQUEST if offer does not meet offer conditions.
     */
    public ResponseEntity<Response<Offer>> createNonTargetedCompanyOffer(
             String userName,
             NonTargetedCompanyOffer nonTargetedCompanyOffer) {

        if (!userName.equals(nonTargetedCompanyOffer.getCompanyId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Response<>(null, "User can not make this offer"));
        }

        return nonTargetedCompanyOfferService.saveOfferWithResponse(nonTargetedCompanyOffer);
    }

    /**
     * Method for posting an application to a NonTargetedCompanyOffer.
     *
     * @param userName    Username of users.
     * @param userRole    Role of user.
     * @param application Application we want to file
     * @param offerId     Offer the application targets.
     * @return ResponseEntity containing either data or an errormessage.
     *          201 CREATED if request successful.
     *          401 UNAUTHORIZED if user is not logged in.
     *          403 FORBIDDEN if user not author of application or not a student.
     *          400 BAD REQUEST if it does not meet application conditions.
     */
    @PostMapping("/apply/{offerId}")
    public ResponseEntity<Response<Application>> apply(
            @RequestHeader(nameHeader) String userName,
            @RequestHeader(roleHeader) String userRole,
            @RequestBody Application application,
            @PathVariable Long offerId) {
        if (userName.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new Response<>(null, "User has not been authenticated"));
        }
        if (!userName.equals(application.getStudentId())
                || !userRole.equals("STUDENT")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Response<>(null, "User can not make this application"));
        }

        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new Response<>(nonTargetedCompanyOfferService
                            .apply(application, offerId)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response<>(null, e.getMessage()));
        }
    }

    /**
     * Endpoint for accepting an application by a company.
     *
     * @param userName - the name of the user.
     *                 Both are contained in the JWT.
     * @return - a Response, containing a success or an error message!
     */
    public ResponseEntity<Response<ContractDto>> acceptApplication(
             String userName,
             Long id) {

        try {
            ContractDto contract = nonTargetedCompanyOfferService.accept(userName, id);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new Response<>(contract,
                            "Application has been accepted successfully!"));
        } catch (NoPermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Response<>(null, e.getMessage()));
        } catch (IllegalArgumentException | ContractCreationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response<>(null, e.getMessage()));
        }
    }
}
