package com.sellics.amazon.controller;


import com.sellics.amazon.utils.AmazonAutocompleteApi;
import com.sellics.amazon.calculation.AlgorithmSettings;
import com.sellics.amazon.calculation.SearchVolumeIterator;
import com.sellics.amazon.models.Estimation;
import com.sellics.amazon.utils.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.BiFunction;

import static java.util.Arrays.asList;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.status;
import static org.springframework.web.bind.annotation.RequestMethod.GET;


/**
 * @author Chaklader on 2020-02-07
 */
@RestController
public class EstimationController {

    private String stopWords;
    private BiFunction<AlgorithmSettings, String, SearchVolumeIterator> factory;
    private AmazonAutocompleteApi amazonApi;

    private int maxMatchesPerApiResponse;
    private Time time;
    private long maximumAllowedRunningTime;


    @Autowired
    public EstimationController(@Value("${com.sellics.amazon.calculation.stopwords}") String stopWords,
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
     * @param keyword    A required parameter that is going to be used as the search term
     * @param market     optional parameter to select the marketplace by code. If empty it is going to default to the value set in  com.sellics.amazon.controller.default_mkt
     * @param department optional parameter to filter by department. Defaults to the value set in com.sellics.amazon.controller.default_department
     *                   This endpoint will run a itterative calculation in the time available as set by om.sellics.amazon.controller.runningtime_in_nanoseconds. If the time runs out it will return a score but the status code will be 504.
     *                   If the calculation finishes in time, the status will be 200.
     * @return Estimation consisting of the score, and the search term
     */
    @RequestMapping(path = "/estimate", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Estimation> estimate(@RequestParam(value = "keyword") String keyword,
                                               @RequestParam(value = "market", required = false, defaultValue = "${com.sellics.amazon.controller.default_mkt}") String market,
                                               @RequestParam(value = "department", required = false, defaultValue = "${com.sellics.amazon.controller.default_department}") String department) {

        SearchVolumeIterator estimationAlgorithm = createAlgorithm(keyword, market, department);
        int score = tryRun(estimationAlgorithm);

        Estimation estimation = new Estimation(score, keyword);
        HttpStatus status = estimationAlgorithm.hasNext() ? GATEWAY_TIMEOUT : OK;
        return status(status).body(estimation);

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

    private boolean willTimeOut(double averageItterationDuration, Long startTime) {
        return time.now() - startTime > maximumAllowedRunningTime - averageItterationDuration;
    }

    private SearchVolumeIterator createAlgorithm(String keyword, String market, String department) {
        AlgorithmSettings algorithmSettings;
        algorithmSettings = AlgorithmSettings.builder()
                .matchFinder(term -> amazonApi.autocomplete(term, market, department).getMatches())
                .maxResultSetSize(maxMatchesPerApiResponse)
                .suffixStopWords(asList(stopWords.split(",")))
                .build();
        return factory.apply(algorithmSettings, keyword);
    }


}