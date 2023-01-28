package nl.tudelft.sem.template.controllers;

import java.util.List;
import nl.tudelft.sem.template.entities.Offer;
import nl.tudelft.sem.template.entities.TargetedCompanyOffer;
import nl.tudelft.sem.template.entities.dtos.Response;
import nl.tudelft.sem.template.exceptions.UserNotAuthorException;
import nl.tudelft.sem.template.services.TargetedCompanyOfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TargetedCompanyOfferController {

    @Autowired
    private transient TargetedCompanyOfferService targetedCompanyOfferService;

    private final transient String nameHeader = "x-user-name";
    private final transient String roleHeader = "x-user-role";
    private final transient String authenticationError = "User is not authenticated";

    /** Endpoint for creating TargetedCompanyOffers.
     *
     * @param userName Name of person making request.
     * @param targetedCompanyOffer TargetedCompanyOffer that needs to be saved.
     * @param id id of StudentOffer that TargetedCompanyOffer targets.
     * @return ResponseEntity that can take various codes.
     *          401 OK if user not authenticated.
     *          403 UNAUTHORIZED if user not Company or not author of offer.
     *          400 BAD REQUEST is conditions are not met.
     *          201 CREATED if successful with offer in the body.
     */
    public ResponseEntity<Response<Offer>> saveTargetedCompanyOffer(
            String userName,
            TargetedCompanyOffer targetedCompanyOffer,
            Long id) {

        if (!userName.equals(targetedCompanyOffer.getCompanyId())) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new Response<>(null,
                            "You can only post an offer for your own company"));
        }
        return targetedCompanyOfferService.saveOfferWithResponse(targetedCompanyOffer, id);
    }

    /** Endpoint for getting TargetedCompanyOffers by a company, which created them.
     *
     * @param userName Name of person making the request.
     * @return ResponseEntity that can take various codes.
     *          401 UNAUTHORIZED if user not authenticated.
     *          403 FORBIDDEN if user not a company.
     *          200 OK if successful with List in body.
     */
    public ResponseEntity<Response<List<Offer>>>
        getCompanyOffersById(String userName) {

        Response<List<Offer>> responseOffersById;
        try {
            responseOffersById =
                    new Response<>(
                            targetedCompanyOfferService.getOffersById(userName),
                            null);

            return ResponseEntity.ok(responseOffersById);
        } catch (IllegalArgumentException exception) {
            exception.printStackTrace();

            responseOffersById =
                new Response<>(
                    null,
                    exception.getMessage());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(responseOffersById);
        }
    }

    /**
     * Endpoint for getting TargetedCompanyOffers by a StudentOffer.
     *
     * @param userName Name of user making the request.
     * @param studentOfferId - the offer's id.
     * @return ResponseEntity that can take various codes.
     *          401 UNAUTHORIZED if not authenticated.
     *          403 FORBIDDEN if studentOffer doesn't belong to user making request.
     *          200 OK if successful with list of offers that belong to the StudentOffer.
     *          400 BAD REQUEST if conditions are not met.
     */
    public ResponseEntity<Response<List<Offer>>>
        getCompanyOffersByStudentOffer(String userName,
                                       Long studentOfferId) {

        Response<List<Offer>> offers;
        try {
            offers =
                    new Response<>(
                            targetedCompanyOfferService
                                    .getOffersByStudentOffer(studentOfferId, userName),
                            null);

            return ResponseEntity.ok(offers);
        } catch (IllegalArgumentException exception) {
            exception.printStackTrace();

            offers =
                new Response<>(
                    null,
                    exception.getMessage()
                );
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(offers);
        } catch (UserNotAuthorException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new Response<>(null, e.getMessage()));
        }
    }

    /**
     * Endpoint for getting al TargetedCompanyOffers of a student.
     *
     * @param userName Name of the user making request.
     * @param userRole Role of the user making request.
     * @return ResponseEntity that can take various codes.
     *          401 UNAUTHORIZED if user not authenticated.
     *          403 FORBIDDEN if user not a student.
     *          400 BAD REQUEST if conditions of the request are not met.
     *          200 OK if successful with a List of TargetedCompanyRequests in body.
     */
    @GetMapping("/company/targeted/student/")
    public ResponseEntity<Response<List<TargetedCompanyOffer>>>
        getAllByStudent(@RequestHeader(nameHeader) String userName,
                        @RequestHeader(roleHeader) String userRole) {
        if (userName.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new Response<>(null, authenticationError));
        }
        if (!userRole.equals("STUDENT")) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new Response<>(null, "User is not a student"));
        }
        Response<List<TargetedCompanyOffer>> offers = new Response<>(
                            targetedCompanyOfferService
                                    .getAllByStudent(userName),
                            null);

        return new ResponseEntity<>(
            offers,
            HttpStatus.OK);

    }
}
