package com.chenkh.comicreader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@Configuration
public class ComicreaderApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        System.setProperty("jsonPath", args[0]);
        System.setProperty("imgPath", args[1]);
        SpringApplication.run(ComicreaderApplication.class, args);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/comic/booklist.html");
    }
    
}
