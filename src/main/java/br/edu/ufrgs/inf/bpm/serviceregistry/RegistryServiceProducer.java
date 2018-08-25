package br.edu.ufrgs.inf.bpm.serviceregistry;

import br.edu.ufrgs.inf.bpm.wrapper.WordNetWrapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient

// @SpringBootApplication
// @EnableEurekaClient
public class RegistryServiceProducer {

    public static void main(String[] args) {
        initializeService();
        SpringApplication.run(RegistryServiceProducer.class, args);
    }

    public static void initializeService() {
        WordNetWrapper.generateDictionary();
    }

}
