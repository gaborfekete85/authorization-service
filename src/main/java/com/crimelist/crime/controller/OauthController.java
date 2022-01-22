package com.crimelist.crime.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth2")
public class OauthController {

    @GetMapping("/test")
    public String test() {
        return "Hello World";
    }

}
