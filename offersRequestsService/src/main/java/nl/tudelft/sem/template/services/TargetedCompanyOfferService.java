package nl.tudelft.sem.template.services;

import java.util.List;
import nl.tudelft.sem.template.entities.Offer;
import nl.tudelft.sem.template.entities.StudentOffer;
import nl.tudelft.sem.template.entities.TargetedCompanyOffer;
import nl.tudelft.sem.template.entities.dtos.Response;
import nl.tudelft.sem.template.exceptions.UserNotAuthorException;
import nl.tudelft.sem.template.repositories.StudentOfferRepository;
import nl.tudelft.sem.template.repositories.TargetedCompanyOfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class TargetedCompanyOfferService extends OfferService {

    @Autowired
    private transient TargetedCompanyOfferRepository targetedCompanyOfferRepository;

    @Autowired
    private transient StudentOfferRepository studentOfferRepository;


    /**
     * Method for saving a TargetedCompanyOffer.
     *
     * @param targetedCompanyOffer Offer we want to save.
     * @param id                   Long with the StudentOffer this CompanyOffer targets.
     * @return The saved Offer.
     * @throws IllegalArgumentException Thrown if any of the conditions are not met.
     */
    public ResponseEntity<Response<Offer>> saveOfferWithResponse(
            TargetedCompanyOffer targetedCompanyOffer, Long id) {
        StudentOffer studentOffer = studentOfferRepository.getById(id);
        if (studentOffer == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response<>(null, "Student offer does not exist"));
        }
        targetedCompanyOffer.setStudentOffer(studentOffer);
        return super.saveOfferWithResponse(targetedCompanyOffer);
    }

    /**
     * Service, which provides Targeted offers, created by a specific Company.
     *
     * @param companyId - the ID of the Company.
     * @return - A list of Targeted Requests, which are all created by the Company.
     */
    public List<Offer> getOffersById(String companyId) {
        List<Offer> offers =
            targetedCompanyOfferRepository.findAllByCompanyId(companyId);
        if (offers.size() == 0) {
            throw new IllegalArgumentException("No such company has made offers!");
        }

        return offers;
    }

    /** Service, which provides Targeted offers, related to a specific Student offer.
     *
     * @param studentOfferId - the id of the Student offer that we want to specify.
     * @param username Name of the person making the request.
     * @return List of TargetedCompanyOffers belonging to the StudentOffer.
     */
    public List<Offer> getOffersByStudentOffer(Long studentOfferId,
                                                              String username)
            throws UserNotAuthorException {
        StudentOffer studentOffer = studentOfferRepository.getById(studentOfferId);
        if (studentOffer == null) {
            throw new IllegalArgumentException("Student offer does not exist");
        }
        if (!username.equals(studentOffer.getStudentId())) {
            throw new UserNotAuthorException(username);
        }
        return targetedCompanyOfferRepository.findAllByStudentOffer(studentOffer);
    }

    /**
     * Service, which provides a list of offers, which target a specific student.
     *
     * @param student - the targeted student.
     * @return - a list of TargetedCompanyOffers.
     */
    public List<TargetedCompanyOffer> getAllByStudent(String student) {
        return targetedCompanyOfferRepository.getAllByStudent(student);
    }
}
