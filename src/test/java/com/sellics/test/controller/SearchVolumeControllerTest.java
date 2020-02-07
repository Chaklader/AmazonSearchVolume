package com.sellics.test.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sellics.test.models.SearchVolume;
import com.sellics.test.models.Time;
import com.sellics.test.services.SearchVolumeService;
import com.sellics.test.utils.AutocompleteResultsInterface;
import com.sellics.test.utils.AutocompleteResults;
import com.sellics.test.calculation.SearchVolumeIterator;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Arrays.asList;

/**
 * @author Chaklader on 2020-02-07
 */
public class SearchVolumeControllerTest {

    public static final String arbitraryStopWords = " ";
    public static final int MAX_MATCHES_PER_API_RESPONSE = 10;
    public static final String ARBITRARY_KEYWORD = "arbitraryKeyword";

    public static final String ARBITARY_MARKET = "1";
    public static final String ARBITARY_DEPARTMENT = "2";

    public SearchVolumeIterator infiniteLoop = new SearchVolumeIterator() {

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public Integer next() {
            return 0;
        }
    };

    public AutocompleteResultsInterface appleHeadphonesMockAutocompleteApi = (String searchTerm, String market, String department) -> {

        try {
            JsonNode[] jsonNodes = new ObjectMapper().readValue(SearchVolumeControllerTest.class.getResourceAsStream("/sampleApiResponse.json"), JsonNode[].class);
            return new AutocompleteResults(asList(jsonNodes));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };

    @Test
    public void givenLongRequestAfterAtLeastOneIteration_estimate_returnsErrorCodeAndCorrectScore() {

        long allowedRunningTime = 10;
        AtomicLong runningTime = new AtomicLong(0);

        Time time = () -> {
            long result = runningTime.getAndAdd(allowedRunningTime / 2);
            return result;
        };

        int expectedResult = 2;

        SearchVolumeIterator algorithm = getIterativeEstimatiorForFixedResultAndFIxedNumberOfIterations(expectedResult, 10000);

        SearchVolumeService service = new SearchVolumeService(arbitraryStopWords, MAX_MATCHES_PER_API_RESPONSE, allowedRunningTime, (settings, keyword) -> algorithm, appleHeadphonesMockAutocompleteApi, time);
        SearchVolumeController searchVolumeController = new SearchVolumeController(service);

        ResponseEntity<SearchVolume> calculate = searchVolumeController.calculate(ARBITRARY_KEYWORD, ARBITARY_MARKET, ARBITARY_DEPARTMENT);

        Assert.assertEquals(HttpStatus.GATEWAY_TIMEOUT, calculate.getStatusCode());
        Assert.assertEquals(expectedResult, calculate.getBody().getScore());
        Assert.assertEquals(ARBITRARY_KEYWORD, calculate.getBody().getKeyword());
    }

    @Test
    public void givenLongRequestAfterNoIterations_estimate_returnsErrorCodeAndScoreZero() {

        long allowedRunningTime = 10;

        AtomicLong runningTime = new AtomicLong(0);
        Time time = () -> {
            long res = runningTime.getAndAdd(allowedRunningTime * 2);
            return res;
        };

        int estimatedResult = 2;
        int expectedResult = 0;

        SearchVolumeIterator algorithm = getIterativeEstimatiorForFixedResultAndFIxedNumberOfIterations(estimatedResult, 10000);

        SearchVolumeService service = new SearchVolumeService(arbitraryStopWords, MAX_MATCHES_PER_API_RESPONSE, allowedRunningTime, (settings, keyword) -> algorithm, appleHeadphonesMockAutocompleteApi, time);
        SearchVolumeController searchVolumeController = new SearchVolumeController(service);

        ResponseEntity<SearchVolume> calculate = searchVolumeController.calculate(ARBITRARY_KEYWORD, ARBITARY_MARKET, ARBITARY_DEPARTMENT);

        Assert.assertEquals(HttpStatus.GATEWAY_TIMEOUT, calculate.getStatusCode());
        Assert.assertEquals(expectedResult, calculate.getBody().getScore());
        Assert.assertEquals(ARBITRARY_KEYWORD, calculate.getBody().getKeyword());
    }

    @Test
    public void givenNormalRunningTime_estimate_returnsOkCode() {

        long allowedRunningTime = 10;
        Time time = () -> 1;
        int expectedResult = 3;

        SearchVolumeIterator algorithm = getIterativeEstimatiorForFixedResultAndFIxedNumberOfIterations(expectedResult, 2);

        SearchVolumeService service = new SearchVolumeService(arbitraryStopWords, MAX_MATCHES_PER_API_RESPONSE, allowedRunningTime, (settings, keyword) -> algorithm, appleHeadphonesMockAutocompleteApi, time);
        SearchVolumeController searchVolumeController = new SearchVolumeController(service);

        ResponseEntity<SearchVolume> calculate = searchVolumeController.calculate(ARBITRARY_KEYWORD, ARBITARY_MARKET, ARBITARY_DEPARTMENT);

        Assert.assertEquals(HttpStatus.OK, calculate.getStatusCode());
        Assert.assertEquals(expectedResult, calculate.getBody().getScore());
        Assert.assertEquals(ARBITRARY_KEYWORD, calculate.getBody().getKeyword());
    }

    public SearchVolumeIterator getIterativeEstimatiorForFixedResultAndFIxedNumberOfIterations(int res, int maxIterations) {

        AtomicInteger iteration = new AtomicInteger(0);

        return new SearchVolumeIterator() {

            @Override
            public boolean hasNext() {
                return iteration.get() < maxIterations;
            }

            @Override
            public Integer next() {
                iteration.incrementAndGet();
                return res;
            }
        };
    }

}
