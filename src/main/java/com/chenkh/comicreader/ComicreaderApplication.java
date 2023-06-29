package com.chenkh.comicreader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

@SpringBootApplication
public class ComicreaderApplication {

    public static void main(String[] args) {
        String jsonPath = System.getProperty("jsonPath");
        String imgPath = System.getProperty("imgPath");
        if (!StringUtils.hasLength(jsonPath) || !StringUtils.hasLength(imgPath)) {
            System.out.println("jsonPath and imgPath is empty");
            return;
        }
        
        SpringApplication.run(ComicreaderApplication.class, args);
    }

}
