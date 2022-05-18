package com.searcher.p_searcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;

@SpringBootApplication
@Controller
public class PSearcherApplication {

    public static void main(String[] args) {
        SpringApplication.run(PSearcherApplication.class, args);
    }

}
