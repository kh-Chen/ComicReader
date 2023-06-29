package com.chenkh.comicreader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ComicreaderApplication {

    public static void main(String[] args) {
        System.setProperty("jsonPath", args[0]);
        System.setProperty("imgPath", args[1]);
        SpringApplication.run(ComicreaderApplication.class, args);
    }

}
