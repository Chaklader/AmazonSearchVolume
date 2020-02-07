package com.sellics.test.services;

import com.sellics.test.calculation.Settings;
import com.sellics.test.calculation.SearchVolumeIterator;
import com.sellics.test.utils.AutocompleteResultsInterface;
import com.sellics.test.models.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;
import static java.util.Arrays.asList;


/**
 * @author Chaklader on 2020-02-07
 */
@Service
public class SearchVolumeService {

    private String stopWords;
    private BiFunction<Settings, String, SearchVolumeIterator> factory;
    private AutocompleteResultsInterface amazonApi;

    private int maxMatchesPerApiResponse;
    private Time time;
    private long maximumAllowedRunningTime;


    @Autowired
    public SearchVolumeService(@Value("${com.sellics.amazon.calculation.stopwords}") String stopWords,
                               @Value("${com.sellics.core.amazon.max_matches_per_response}") int maxMatchesPerApiResponse,
                               @Value("${com.sellics.restapi.runningtime_in_nanoseconds}") long maximumAllowedRunningTime,
                               BiFunction<Settings, String, SearchVolumeIterator> factory,
                               AutocompleteResultsInterface amazonApi,
                               Time time) {

        this.stopWords = stopWords;
        this.factory = factory;
        this.amazonApi = amazonApi;

        this.time = time;
        this.maxMatchesPerApiResponse = maxMatchesPerApiResponse;
        this.maximumAllowedRunningTime = maximumAllowedRunningTime;
    }

    public int runSearchVolumeIterator(SearchVolumeIterator searchVolumeIterator) {

        double avgIterationDuration = 0;
        int count = 0;

        Long startTime = time.now();
        int result = 0;

        while (searchVolumeIterator.hasNext() && !isTimeOut(avgIterationDuration, startTime)) {

            result = searchVolumeIterator.next();
            count++;

            avgIterationDuration = (time.now() - startTime) / count;
        }

        return result;
    }

    public boolean isTimeOut(double averageItterationDuration, Long startTime) {
        return time.now() - startTime > maximumAllowedRunningTime - averageItterationDuration;
    }

    public SearchVolumeIterator create(String keyword, String market, String department) {

        Settings settings = Settings.builder()
                .matchFinder(term -> amazonApi.autocomplete(term, market, department).getMatches())
                .maxResultSetSize(maxMatchesPerApiResponse)
                .suffixStopWords(asList(stopWords.split(",")))
                .build();

        return factory.apply(settings, keyword);
    }

}
