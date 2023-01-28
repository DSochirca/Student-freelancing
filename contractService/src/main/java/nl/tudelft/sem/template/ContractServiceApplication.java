package nl.tudelft.sem.template;

import logger.FileLogger;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@EnableEurekaClient
@SpringBootApplication
public class ContractServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContractServiceApplication.class, args);
    }

    @Bean
    @LoadBalanced
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    /** Bearn for the FileLogger.
     *
     * @return FileLogger instance.
     */
    @Bean
    public FileLogger getLogger() {
        FileLogger fileLogger = FileLogger.getInstance();
        fileLogger.init("contractService");
        return fileLogger;
    }
}
