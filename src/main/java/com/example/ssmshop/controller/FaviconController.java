package com.example.ssmshop.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FaviconController {
    @GetMapping(value = "/favicon.ico", produces = "image/svg+xml")
    public Resource favicon() {
        return new ClassPathResource("static/favicon.svg");
    }
}
