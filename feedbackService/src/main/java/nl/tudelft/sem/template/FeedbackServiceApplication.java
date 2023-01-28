package nl.tudelft.sem.template;

import logger.FileLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@EnableEurekaClient
@SpringBootApplication
public class FeedbackServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeedbackServiceApplication.class, args);
    }

    @Bean
    @LoadBalanced
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    /** Bean for the FileLogger.
     *
     * @return FileLogger instance.
     */
    @Bean
    public FileLogger getLogger() {
        FileLogger fileLogger = FileLogger.getInstance();
        fileLogger.init("feedbackService");
        return fileLogger;
    }
}
