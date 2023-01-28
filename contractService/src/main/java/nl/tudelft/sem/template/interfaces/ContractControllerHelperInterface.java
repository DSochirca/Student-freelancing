package nl.tudelft.sem.template.interfaces;

import org.springframework.http.ResponseEntity;

public interface ContractControllerHelperInterface {
    public ResponseEntity<Object> getResponseEntityForException(Exception e);
}
