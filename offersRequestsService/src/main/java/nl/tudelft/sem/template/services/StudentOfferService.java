package nl.tudelft.sem.template.services;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import javax.naming.NoPermissionException;
import javax.transaction.Transactional;
import logger.FileLogger;
import nl.tudelft.sem.template.entities.Offer;
import nl.tudelft.sem.template.entities.StudentOffer;
import nl.tudelft.sem.template.entities.TargetedCompanyOffer;
import nl.tudelft.sem.template.entities.dtos.ContractDto;
import nl.tudelft.sem.template.enums.Status;
import nl.tudelft.sem.template.exceptions.ContractCreationException;
import nl.tudelft.sem.template.exceptions.UserDoesNotExistException;
import nl.tudelft.sem.template.exceptions.UserServiceUnvanvailableException;
import nl.tudelft.sem.template.repositories.StudentOfferRepository;
import nl.tudelft.sem.template.repositories.TargetedCompanyOfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentOfferService extends OfferService {

    @Autowired
    private transient StudentOfferRepository studentOfferRepository;
    @Autowired
    private transient TargetedCompanyOfferRepository targetedCompanyOfferRepository;
    @Autowired
    private transient Utility utility;
    @Autowired
    private transient FileLogger logger;

    /**
     * Service, which returns all active StudentOffers, which are stored in the repository.
     *
     * @return - A list of Pending Student Offers.
     */
    public List<StudentOffer> getOffers() {
        return studentOfferRepository.findAllActive();
    }

    /**
     * Service, which returns all offers created by a Student.
     *
     * @param studentId - the ID of the Student.
     * @return - A list of the Student's Offers.
     */
    public List<Offer> getOffersById(String studentId)
            throws UserDoesNotExistException, UserServiceUnvanvailableException {
        utility.userExists(studentId);


        List<Offer> offer = studentOfferRepository.findAllByStudentId(studentId);
        if (offer.isEmpty()) {
            throw new IllegalArgumentException("No such student has made offers!");
        }
        return offer;
    }

    /**
     * Service, which accepts the given targeted offer and declines all others.
     *
     * @param userName - the id of the Student, who wants to accept the offer.
     * @param targetedCompanyOfferId - the id of the accepted offer.
     * @throws NoPermissionException - is thrown
     *      if the user doesn't have permission to accept the offer.
     * @throws ContractCreationException - if the request wasn't successful.
     */
    @Transactional
    public ContractDto acceptOffer(
            String userName, Long targetedCompanyOfferId)
            throws NoPermissionException, ContractCreationException {
        TargetedCompanyOffer targetedCompanyOffer =
                targetedCompanyOfferRepository
                        .findById(targetedCompanyOfferId)
                        .orElseThrow(() -> new IllegalArgumentException("ID is not valid!"));
        StudentOffer offer = targetedCompanyOffer.getStudentOffer();

        validateOffer(targetedCompanyOffer, offer, userName);

        // First try to create the contract between the 2 parties.
        // If the contract creation doesn't succeed then the offer isn't accepted.
        // Throws exception if error:
        ContractDto contract = utility.createContract(targetedCompanyOffer.getCompanyId(),
                userName, targetedCompanyOffer.getHoursPerWeek(),
                targetedCompanyOffer.getTotalHours(), offer.getPricePerHour());

        saveAcceptance(targetedCompanyOffer, offer);

        return contract;
    }

    /**
     * This method checks whether the user and the offer are valid.
     *
     * @param targetedCompanyOffer - the targeted offer, which is to be accepted.
     * @param offer - the student offer.
     * @param userName - the name of the user, who wants to accept.
     * @throws NoPermissionException - May throw it, if the user,
     *      who wants to accept is not the creator of the student offer.
     */
    private void validateOffer(
            TargetedCompanyOffer targetedCompanyOffer,
            StudentOffer offer, String userName)
            throws NoPermissionException {
        if (!offer
                .getStudentId()
                .equals(userName)) {
            throw new NoPermissionException("User not allowed to accept this TargetedOffer");
        }
        if (offer.getStatus() != Status.PENDING
                || targetedCompanyOffer.getStatus() != Status.PENDING) {
            throw new IllegalArgumentException(
                    "The StudentOffer or TargetedRequest is not active anymore!");
        }
    }

    /**
     * This method applies status changes and stores the changes in the database.
     *
     * @param targetedCompanyOffer - the offer, which will be accepted.
     * @param offer - the student offer.
     */
    private void saveAcceptance(TargetedCompanyOffer targetedCompanyOffer,
                                StudentOffer offer) {
        List<TargetedCompanyOffer> offers = offer.getTargetedCompanyOffers();
        for (TargetedCompanyOffer t : offers) {
            if (!t.equals(targetedCompanyOffer)) {
                t.setStatus(Status.DECLINED);
            } else {
                t.setStatus(Status.ACCEPTED);
            }
            targetedCompanyOfferRepository.save(t);
        }

        logger.log(offer.getCreatorUsername()
                + " has accepted offer"
                + targetedCompanyOffer.getId()
                + " from user "
                + targetedCompanyOffer.getCreatorUsername());
        offer.setStatus(Status.DISABLED);
        studentOfferRepository.save(offer);
    }

    /**
     * Service, which updates a StudentOffer.
     *
     * @param studentOffer - The updated offer, which will be now stored.
     */
    public void updateStudentOffer(StudentOffer studentOffer) {
        StudentOffer current =  studentOfferRepository.getById(studentOffer.getId());
        if (current == null) {
            throw new IllegalArgumentException("This StudentOffer does not exist!");
        }
        if (current.getStatus() != studentOffer.getStatus()) {
            throw new IllegalArgumentException("You are not allowed to edit the Status");
        }

        super.saveOfferWithResponse(studentOffer);
    }

    /**
     * Service, which gets all offers, whose fields are equal to the keyword.
     *
     * @param keyWord - the parameter we filter by.
     * @return - a list of Student Offers.
     * @throws UnsupportedEncodingException  -is thrown, if the input is invalid.
     */
    public List<StudentOffer> getByKeyWord(String keyWord) throws UnsupportedEncodingException {
        String decoded = URLDecoder.decode(keyWord, StandardCharsets.UTF_8);

        return studentOfferRepository.getAllByKeyWord(decoded);
    }

    /**
     * Service, which gets all offers, whose expertises contain the criteria.
     *
     * @param expertises - the parameter we filter by.
     * @return - a list of Student Offers.
     * @throws UnsupportedEncodingException  -is thrown, if the input is invalid.
     */
    public List<StudentOffer> getByExpertises(List<String> expertises)
            throws UnsupportedEncodingException {
        for (int i = 0; i < expertises.size(); i++) {
            String decoded =
                    URLDecoder.decode(expertises.get(i), StandardCharsets.UTF_8);
            expertises.set(i, decoded);
        }

        List<StudentOffer> offers = studentOfferRepository.findAllActive();
        offers.removeIf(offer -> Collections.disjoint(offer.getExpertise(), expertises));

        return offers;
    }
}
