package com.urbe.ms_auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestAutController {

    @GetMapping("/hello")
    public String hello(){
        return "Hello World";
    }


    @GetMapping("/hello-secured")
    public String helloSecured(){
        return "hello World Secured";
    }

}
