package br.edu.ufrgs.inf.bpm.service;

import br.edu.ufrgs.inf.bpm.wrapper.WordNetWrapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class RegistryServiceProvider {

    public static void main(String[] args) {
        initializeService();
        SpringApplication.run(RegistryServiceProvider.class, args);
    }

    public static void initializeService() {
        WordNetWrapper.generateDictionary();
    }

}
