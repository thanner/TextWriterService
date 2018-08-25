package br.edu.ufrgs.inf.bpm.serviceregistry;

import org.springframework.stereotype.Service;

//import io.swagger.annotations.Api;

// @Api("/sayHello")
@Service
public class HelloServiceImpl1 implements HelloService {

    public String sayHello() {
        return "Hello, Welcome to CXF RS Spring Boot World!!!";
    }

}