package com.sellics.challenge.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sellics.challenge.calculation.Settings;
import com.sellics.challenge.calculation.SearchVolumeCountAlgorithm;
import com.sellics.challenge.calculation.SearchVolumeIterator;
import com.sellics.challenge.models.Time;

import com.sellics.challenge.utils.AutocompleteResultsInterface;
import com.sellics.challenge.utils.RestAutocompleteApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.function.BiFunction;

/**
 * @author Chaklader on 2020-02-07
 */
@Configuration
public class AppConfiguration {

    @Bean
    public Time timestampProvider() {
        return System::nanoTime;
    }

    @Bean
    @Autowired
    AutocompleteResultsInterface amazonAutocompleteApi(RestOperations restOperations, @Value("${com.sellics.core.amazon.url}") String amazonUrl, ObjectMapper objectMapper) {
        return new RestAutocompleteApi(restOperations, amazonUrl, objectMapper);
    }

    @Bean
    RestOperations restOperations() {
        return new RestTemplate();
    }

    @Bean
    BiFunction<Settings, String, SearchVolumeIterator> algorithmFactory() {
        return SearchVolumeCountAlgorithm::new;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
