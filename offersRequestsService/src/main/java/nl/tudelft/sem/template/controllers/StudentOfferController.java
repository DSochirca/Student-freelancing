package nl.tudelft.sem.template.controllers;

import java.io.UnsupportedEncodingException;
import java.util.List;
import javax.naming.NoPermissionException;
import nl.tudelft.sem.template.entities.Offer;
import nl.tudelft.sem.template.entities.StudentOffer;
import nl.tudelft.sem.template.entities.dtos.ContractDto;
import nl.tudelft.sem.template.entities.dtos.Response;
import nl.tudelft.sem.template.exceptions.ContractCreationException;
import nl.tudelft.sem.template.exceptions.UserDoesNotExistException;
import nl.tudelft.sem.template.exceptions.UserServiceUnvanvailableException;
import nl.tudelft.sem.template.services.StudentOfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudentOfferController {

    @Autowired
    private transient StudentOfferService studentOfferService;

    private final transient String nameHeader = "x-user-name";
    private final transient String roleHeader = "x-user-role";
    private final transient String roleStudent = "STUDENT";
    private final transient String roleCompany = "COMPANY";
    private final transient String unauthenticatedMessage
            = "User has not been authenticated";

    /** Endpoint for creating StudentOffers.
     *
     * @param userName Name of person making the request.
     * @param studentOffer StudentOffer than needs to bed created.
     * @return ResponseEntity that can take various codes.
     *          401 UNAUTHORIZED if user not authenticated.
     *          403 FORBIDDEN if user not a student or not author of offer.
     *          400 BAD REQUEST if conditions for offer are not met.
     *          201 CREATED with Offer in body if successful.
     */
    public ResponseEntity<Response<Offer>>
        saveStudentOffer(@RequestHeader(nameHeader) String userName,
                         @RequestBody StudentOffer studentOffer) {

        if (!studentOffer.getStudentId().equals(userName)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new Response<>(null, "User not allowed to post this StudentOffer"));
        }
        return studentOfferService.saveOfferWithResponse(studentOffer);
    }

    /**
     * Endpoint for getting all StudentOffers.
     *
     * @return 200 OK ResponseEntity with a Response with a list of StudentOffers
     *     if the requester has permission.
     *     It is to be decided how the authentication will handle the other case.
     */
    @GetMapping("/student/getAllOffers")
    public ResponseEntity<Response<List<StudentOffer>>>
        getAllStudentOffers() {
        // We have to check if the requester has the rights to view the student offers.

        Response<List<StudentOffer>> responseStudentOffers =
            new Response<>(studentOfferService.getOffers(), null);
        return ResponseEntity.ok(responseStudentOffers);
    }

    /**
     * Endpoint for getting all offers of the same student.
     *
     * @param studentId the id of the student.
     * @return 200 OK ResponseEntity with a Response
     *     which contains list of StudentOffers if valid
     *     and 400 BAD Request with a Response containing error message otherwise.
     */
    public ResponseEntity<Response<List<Offer>>>
        getStudentOffersById(String studentId) {

        Response<List<Offer>> responseOffersById;
        try {
            responseOffersById =
                    new Response<>(studentOfferService
                            .getOffersById(studentId),
                            null);

            return ResponseEntity.ok(responseOffersById);
        } catch (IllegalArgumentException exception) {
            exception.printStackTrace();

            responseOffersById =
                    new Response<>(null, exception.getMessage());

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(responseOffersById);
        } catch (UserServiceUnvanvailableException e) {
            return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new Response<>(null, e.getMessage()));
        } catch (UserDoesNotExistException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new Response<>(null, e.getMessage()));
        }
    }

    /**
     * Endpoint, which accepts a Targeted Offer.
     *
     * @param userName - the name of the user.
     * @param id - the id of the offer, which the user wants to be accepted.
     * @return - A Response with a success or an error message!
     */
    public ResponseEntity<Response<ContractDto>>
        acceptTargetedOffer(
             String userName,
             Long id) {

        try {
            ContractDto contract = studentOfferService.acceptOffer(userName, id);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new Response<>(contract, "The Company Offer was accepted successfully!"));
        } catch (NoPermissionException exception) {
            exception.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new Response<>(null, exception.getMessage()));
        } catch (IllegalArgumentException | ContractCreationException exception) {
            exception.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response<>(null, exception.getMessage()));
        }
    }

    /**
     * Endpoint for editing a StudentOffer.
     *
     * @param studentOffer - the updated offer.
     * @param userName - the username of the requester.
     * @param userRole - the role of the requester.
     * @return - A response, which either contains an error message,
     *      or a success message.
     */
    @PutMapping("student/offer")
    public ResponseEntity<Response<String>>
        editStudentOffer(
                @RequestBody StudentOffer studentOffer,
                @RequestHeader(nameHeader) String userName,
                @RequestHeader(roleHeader) String userRole) {

        if (userName.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new Response<>(null, unauthenticatedMessage));
        }

        if (!userName.equals(studentOffer.getStudentId())
                || !userRole.equals(roleStudent)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Response<>(null, "User is not allowed to edit this offer"));
        }

        try {
            studentOfferService.updateStudentOffer(studentOffer);
            return new ResponseEntity<>(
                    new Response<>("Student Offer has been updated successfully!", null),
                    HttpStatus.OK);
        } catch (IllegalArgumentException exception) {
            return new ResponseEntity<>(
                    new Response<>(null, exception.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Endpoint for getting StudentOffers by keyword.
     *
     * @param keyWord - the word, which should be a property of the offer.
     * @param userName - the username of the requester.
     * @param userRole - the role of the requester.
     * @return - A response, which either contains an error message,
     *      or a list of StudentOffers, which contain the keyword.
     */
    @GetMapping("/student/search/{keyWord}")
    public ResponseEntity<Response<List<StudentOffer>>>
            getOffersByKeyWord(@PathVariable String keyWord,
                               @RequestHeader(nameHeader) String userName,
                               @RequestHeader(roleHeader) String userRole) {
        if (userName.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new Response<>(null, unauthenticatedMessage));
        }

        if (!userRole.equals(roleCompany)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Response<>(null, "User is not allowed to see Student Offers!"));
        }

        try {
            return new ResponseEntity<>(
                    new Response<>(studentOfferService.getByKeyWord(keyWord), null),
                    HttpStatus.OK);
        } catch (UnsupportedEncodingException exception) {
            return new ResponseEntity<>(
                    new Response<>(null, "Keyword is invalid!"),
                    HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Endpoint for getting StudentOffers by expertises.
     *
     * @param expertises - the desired expertises
     * @param userName - the username of the requester.
     * @param userRole - the role of the requester.
     * @return - A response, which either contains an error message,
     *      or a list of StudentOffers, which contain the keyword.
     */
    @GetMapping("/student/search/expertises")
    public ResponseEntity<Response<List<StudentOffer>>>
        getOffersByExpertises(@RequestParam List<String> expertises,
                           @RequestHeader(nameHeader) String userName,
                           @RequestHeader(roleHeader) String userRole) {
        if (userName.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new Response<>(null, unauthenticatedMessage));
        }

        if (!userRole.equals(roleCompany)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Response<>(null, "User is not allowed to see Student Offers!"));
        }

        try {
            return new ResponseEntity<>(
                    new Response<>(studentOfferService.getByExpertises(expertises), null),
                    HttpStatus.OK);
        } catch (UnsupportedEncodingException exception) {
            return new ResponseEntity<>(
                    new Response<>(null, "An expertise is invalid!"),
                    HttpStatus.BAD_REQUEST);
        }
    }
}
