package nl.tudelft.sem.template.controllers;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import nl.tudelft.sem.template.entities.NonTargetedCompanyOffer;
import nl.tudelft.sem.template.entities.Offer;
import nl.tudelft.sem.template.entities.StudentOffer;
import nl.tudelft.sem.template.entities.TargetedCompanyOffer;
import nl.tudelft.sem.template.entities.dtos.ContractDto;
import nl.tudelft.sem.template.entities.dtos.OfferDto;
import nl.tudelft.sem.template.entities.dtos.Response;
import nl.tudelft.sem.template.services.OfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OfferController {

    @Autowired
    private transient OfferService offerService;
    @Autowired
    private transient NonTargetedCompanyOfferController nonTargetedController;
    @Autowired
    private transient TargetedCompanyOfferController targetedController;
    @Autowired
    private transient StudentOfferController studentController;

    private final transient String nameHeader = "x-user-name";
    private final transient String roleHeader = "x-user-role";
    private final transient String unauthenticatedMessage = "User is not authenticated";

    /** Endpoint for getting all offers of a user.
     *
     * @param userName String of the username
     * @return 200 OK ResponseEntity
     *     if correct with a response of all StudentOffers in the body.
     *     401 UNAUTHORIZED if user not authenticated.
     */
    @GetMapping("/")
    public ResponseEntity<Response<Map<String, List<Offer>>>>
        getAllByUsername(@RequestHeader(nameHeader) String userName) {
        if (userName.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new Response<>(null, unauthenticatedMessage));
        }
        Response<Map<String, List<Offer>>> response =
                new Response<>(offerService.getAllByUsername(userName), null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /** Endpoint for creating either Student Offers or NonTargetedOffers.
     *
     * @param userName Name of person making the request.
     * @param userRole Role of the person making the request.
     * @param offer Offer than needs to bed created.
     * @return Response with the created offer or an error message.
     */
    @PostMapping({"/", "/{studentOfferId}"})
    public ResponseEntity<Response<Offer>>
        createOffer(@RequestHeader(nameHeader) String userName,
                     @RequestHeader(roleHeader) String userRole,
                     @PathVariable Optional<Long> studentOfferId,
                     @Valid @RequestBody OfferDto offer) {
        if (userName.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new Response<>(null, unauthenticatedMessage));
        }
        try {
            if (userRole.equals("COMPANY")) {
                return createCompanyOffer(userName, studentOfferId, offer);
            } else if (userRole.equals("STUDENT")) {
                return studentController
                        .saveStudentOffer(userName, offer.toStudentOffer());
            } else {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response<>(null, "The entered offer or credentials are invalid"));
        }
    }

    /**
     * This methods handles the creation of a company offer.
     *
     * @param userName - the name of the user.
     * @param studentOfferId - the id of the student offer (Needed for creating a targeted company offer)
     * @param offer - the offer, which will be created.
     * @return - a ResponseEntity, containing a response
     *      with either the created offer or an error message.
     */
    private ResponseEntity<Response<Offer>>
        createCompanyOffer(String userName, Optional<Long> studentOfferId, OfferDto offer){
        if (studentOfferId.isPresent()) {
            return targetedController
                    .saveTargetedCompanyOffer(userName,
                            offer.toTargetedCompanyOffer(), studentOfferId.get());
        } else {
            return nonTargetedController
                    .createNonTargetedCompanyOffer(userName, offer.toNonTargetedCompanyOffer());
        }
    }

    /**
     * Endpoint for accepting either TargetedOffers or Applications.
     * Depending on the user, who wants to
     *
     * @param userName - the name of the user.
     * @param userRole - the role of the user.
     * @param id - the id of the TargetedOffer/Application.
     * @return - A response, containing a contractDTO.
     */
    @PostMapping("/accept/{id}")
    public ResponseEntity<Response<ContractDto>>
        accept(
            @RequestHeader(nameHeader) String userName,
            @RequestHeader(roleHeader) String userRole,
            @PathVariable Long id) {
        if (userName.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new Response<>(null, unauthenticatedMessage));
        }

        if (Objects.equals(userRole, "STUDENT")) {
            return studentController.acceptTargetedOffer(userName, id);
        } else if (Objects.equals(userRole, "COMPANY")) {
            return nonTargetedController.acceptApplication(userName, id);
        } else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response<>(null, "User has invalid Role"));
        }
    }

    /**
     * Endpoint for  getting Offers by creator, or TargetedOffer by StudentOfferId.
     *
     * @param userName - username of the requester.
     * @param userRole - role of the requester.
     * @param studentOffer - optional id of student offer.
     * @return - Response with a List of Offers or an error message.
     */
    @GetMapping({"/getOffers", "/getOffers/{studentOffer}"})
    public ResponseEntity<Response<List<Offer>>>
        getOffersByCreator(@RequestHeader(nameHeader) String userName,
                       @RequestHeader(roleHeader) String userRole,
                       @PathVariable Optional<Long> studentOffer) {
        if (userName.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new Response<>(null, unauthenticatedMessage));
        }

        if (Objects.equals(userRole, "STUDENT")) {
            if (studentOffer.isPresent()) {
                return targetedController
                        .getCompanyOffersByStudentOffer(userName, studentOffer.get());
            }
            return studentController.getStudentOffersById(userName);
        } else if (Objects.equals(userRole, "COMPANY")) {
            if (studentOffer.isPresent()) {
                return targetedController
                        .getCompanyOffersByStudentOffer(userName, studentOffer.get());
            } else {
                return targetedController.getCompanyOffersById(userName);
            }
        } else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response<>(null, "User has invalid Role"));
        }
    }
}
