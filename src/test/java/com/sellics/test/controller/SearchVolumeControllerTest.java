package com.sellics.test.controller;
/*
 * @author aleksandartrposki@gmail.com
 * @since 14.07.19
 *
 *
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sellics.test.utils.AmazonAutocompleteApi;
import com.sellics.test.utils.AutocompleteResults;
import com.sellics.test.calculation.SearchVolumeIterator;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;

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

    public AmazonAutocompleteApi appleHeadphonesMockAutocompleteApi = (String searchTerm, String market, String department)->{
        try {
            JsonNode[] jsonNodes = new ObjectMapper().readValue(SearchVolumeControllerTest.class.getResourceAsStream("/sampleApiResponse.json"), JsonNode[].class);
            return new AutocompleteResults(asList(jsonNodes));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };

//    @Test
//    public void givenLongRequestAfterAtLeastOneIteration_estimate_returnsErrorCodeAndCorrectScore() {
//        long allowedRunningTime = 10;
//        AtomicLong runningTime = new AtomicLong(0);
//        Time time = ()->{
//            long res = runningTime.getAndAdd(allowedRunningTime /2);
//            return res;
//        };
//        int expectedResult = 2;
//        SearchVolumeIterator algorithm = getIterativeEstimatiorForFixedResultAndFIxedNumberOfIterations(expectedResult, 10000);
//        SearchVolumeController searchVolumeController = new SearchVolumeController(arbitraryStopWords, MAX_MATCHES_PER_API_RESPONSE, allowedRunningTime, (settings, keyword) -> algorithm, appleHeadphonesMockAutocompleteApi, time);
//        ResponseEntity<Estimation> estimate = searchVolumeController.estimate(ARBITRARY_KEYWORD, ARBITARY_MARKET, ARBITARY_DEPARTMENT);
//        Assert.assertEquals(HttpStatus.GATEWAY_TIMEOUT,estimate.getStatusCode());
//        Assert.assertEquals(expectedResult,estimate.getBody().getScore());
//        Assert.assertEquals(ARBITRARY_KEYWORD,estimate.getBody().getKeyword());
//    }

//    @Test
//    public void givenLongRequestAfterNoIterations_estimate_returnsErrorCodeAndScoreZero() {
//        long allowedRunningTime = 10;
//        AtomicLong runningTime = new AtomicLong(0);
//        Time time = ()->{
//            long res = runningTime.getAndAdd(allowedRunningTime * 2);
//            return res;
//        };
//        int estimatedResult = 2;
//        int expectedResult = 0;
//        SearchVolumeIterator algorithm = getIterativeEstimatiorForFixedResultAndFIxedNumberOfIterations(estimatedResult, 10000);
//        SearchVolumeController searchVolumeController = new SearchVolumeController(arbitraryStopWords, MAX_MATCHES_PER_API_RESPONSE, allowedRunningTime, (settings, keyword) -> algorithm, appleHeadphonesMockAutocompleteApi, time);
//
//        ResponseEntity<Estimation> estimate = searchVolumeController.estimate(ARBITRARY_KEYWORD, ARBITARY_MARKET, ARBITARY_DEPARTMENT);
//
//        Assert.assertEquals(HttpStatus.GATEWAY_TIMEOUT,estimate.getStatusCode());
//        Assert.assertEquals(expectedResult,estimate.getBody().getScore());
//        Assert.assertEquals(ARBITRARY_KEYWORD,estimate.getBody().getKeyword());
//    }

//    @Test
//    public void givenNormalRunningTime_estimate_returnsOkCode() {
//        long allowedRunningTime = 10;
//        Time time = ()->1;
//        int expectedResult = 3;
//        SearchVolumeIterator algorithm = getIterativeEstimatiorForFixedResultAndFIxedNumberOfIterations(expectedResult, 2);
//        SearchVolumeController searchVolumeController = new SearchVolumeController(arbitraryStopWords, MAX_MATCHES_PER_API_RESPONSE, allowedRunningTime, (settings, keyword) -> algorithm, appleHeadphonesMockAutocompleteApi, time);
//
//        ResponseEntity<Estimation> estimate = searchVolumeController.estimate(ARBITRARY_KEYWORD, ARBITARY_MARKET, ARBITARY_DEPARTMENT);
//
//        Assert.assertEquals(HttpStatus.OK,estimate.getStatusCode());
//        Assert.assertEquals(expectedResult,estimate.getBody().getScore());
//        Assert.assertEquals(ARBITRARY_KEYWORD,estimate.getBody().getKeyword());
//    }

    public SearchVolumeIterator getIterativeEstimatiorForFixedResultAndFIxedNumberOfIterations(int res, int maxIterations) {
        AtomicInteger iteration = new AtomicInteger(0);
        return new SearchVolumeIterator() {
            @Override
            public boolean hasNext() {
                return iteration.get()<maxIterations;
            }

            @Override
            public Integer next() {
                iteration.incrementAndGet();
                return res;
            }
        };
    }
}
