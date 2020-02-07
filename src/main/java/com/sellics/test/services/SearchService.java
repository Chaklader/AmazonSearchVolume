package com.sellics.test.services;

import com.sellics.test.calculation.AlgorithmSettings;
import com.sellics.test.calculation.SearchVolumeIterator;
import com.sellics.test.utils.AmazonAutocompleteApi;
import com.sellics.test.utils.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;

import static java.util.Arrays.asList;

/**
 * @author Chaklader on 2020-02-07
 */
@Service
public class SearchService {

    private String stopWords;
    private BiFunction<AlgorithmSettings, String, SearchVolumeIterator> factory;
    private AmazonAutocompleteApi amazonApi;

    private int maxMatchesPerApiResponse;
    private Time time;
    private long maximumAllowedRunningTime;


    @Autowired
    public SearchService(@Value("${com.sellics.amazon.calculation.stopwords}") String stopWords,
                         @Value("${com.sellics.atrposki.core.amazon.max_matches_per_response}") int maxMatchesPerApiResponse,
                         @Value("${com.sellics.atrposki.restapi.runningtime_in_nanoseconds}") long maximumAllowedRunningTime,
                         BiFunction<AlgorithmSettings, String, SearchVolumeIterator> factory,
                         AmazonAutocompleteApi amazonApi,
                         Time time) {

        this.stopWords = stopWords;
        this.factory = factory;
        this.amazonApi = amazonApi;

        this.time = time;
        this.maxMatchesPerApiResponse = maxMatchesPerApiResponse;
        this.maximumAllowedRunningTime = maximumAllowedRunningTime;
    }

    /**
     * Tries to execute as many algorithm itterations as possible until the timeout.
     * If either the timeout happens or the iterations come to and end, the last result is returned.
     * This method keeps track of the average itteration run time.
     * A timeout happens when the remaining time is smaller then the average iteration duration.
     *
     * @param searchVolumeIterator the iterative algorithm to be run.
     * @return the core of the last algorithm iteration. Returns 0 if no itterations were run.
     */
    public int tryRun(SearchVolumeIterator searchVolumeIterator) {

        double averageItterationDuration = 0;
        int itterations = 0;

        Long startTime = time.now();
        int result = 0;

        while (searchVolumeIterator.hasNext() && !willTimeOut(averageItterationDuration, startTime)) {

            result = searchVolumeIterator.next();
            itterations++;

            averageItterationDuration = (time.now() - startTime) / itterations;
        }

        return result;
    }

    public boolean willTimeOut(double averageItterationDuration, Long startTime) {
        return time.now() - startTime > maximumAllowedRunningTime - averageItterationDuration;
    }

    public SearchVolumeIterator createAlgorithm(String keyword, String market, String department) {
        AlgorithmSettings algorithmSettings;
        algorithmSettings = AlgorithmSettings.builder()
                .matchFinder(term -> amazonApi.autocomplete(term, market, department).getMatches())
                .maxResultSetSize(maxMatchesPerApiResponse)
                .suffixStopWords(asList(stopWords.split(",")))
                .build();
        return factory.apply(algorithmSettings, keyword);
    }
}
