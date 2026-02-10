package com.nagarro.training.rajesh.az_app.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {

    @Value("${azure.app.service.env.variable}")
    private String envVariable;

    @GetMapping("/hello")
    public String helloWorld() {
        return "Hello World from environment variable: " + envVariable;
    }
}
