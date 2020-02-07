package com.sellics.test;

/*
 * @author aleksandartrposki@gmail.com
 * @since 14.07.19
 *
 *
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sellics.test.utils.AmazonAutocompleteApi;
import com.sellics.test.utils.RestTemplateAutocompleteApi;
import com.sellics.test.calculation.AlgorithmSettings;
import com.sellics.test.calculation.SearchVolumeIterator;
import com.sellics.test.calculation.SearchVolumeCountAlgorithm;
import com.sellics.test.utils.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.function.BiFunction;

/**
 * @author Chaklader on 2020-02-07
 */
@SpringBootApplication
@Configuration
//@EnableAutoConfiguration
public class App {

    
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public Time timestampProvider() {
        return System::nanoTime;
    }

    @Bean
    @Autowired
    AmazonAutocompleteApi amazonAutocompleteApi(RestOperations restOperations, @Value("${com.sellics.atrposki.core.amazon.url}") String amazonUrl, ObjectMapper objectMapper) {
        return new RestTemplateAutocompleteApi(restOperations, amazonUrl, objectMapper);
    }

    @Bean
    RestOperations restOperations() {
        return new RestTemplate();
    }

    @Bean
    BiFunction<AlgorithmSettings, String, SearchVolumeIterator> algorithmFactory() {
        return SearchVolumeCountAlgorithm::new;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
