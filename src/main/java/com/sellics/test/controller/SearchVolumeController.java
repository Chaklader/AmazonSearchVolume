package com.sellics.test.controller;


import com.sellics.test.services.SearchVolumeService;
import com.sellics.test.calculation.SearchVolumeIterator;
import com.sellics.test.models.SearchVolume;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.status;
import static org.springframework.web.bind.annotation.RequestMethod.GET;


/**
 * @author Chaklader on 2020-02-07
 */
@RestController
public class SearchVolumeController {

    private SearchVolumeService service;

    @Autowired
    public SearchVolumeController(SearchVolumeService service) {
        this.service = service;
    }

    /**
     * @param keyword    A required parameter that is going to be used as the search term.
     * @param market     optional parameter to select the marketplace by code. If empty it is going to default to the value set.
     * @param department optional parameter to filter by department. Defaults to the value set.
     *                   <p>
     *                   This endpoint will run a iterative calculation in the time available. If the time runs out it will
     *                   return a score but the status code will be 504. If the calculation finishes in time, the status will
     *                   be 200.
     * @return SearchVolume consisting of the score, and the search term
     */
    @RequestMapping(path = "/search", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<SearchVolume> calculate(@RequestParam(value = "keyword") String keyword,
                                                  @RequestParam(value = "market", required = false, defaultValue = "1") String market,
                                                  @RequestParam(value = "department", required = false, defaultValue = "aps") String department) {

        SearchVolumeIterator searchAlgorithm = service.create(keyword, market, department);
        int score = service.runSearchVolumeIterator(searchAlgorithm);

        SearchVolume searchVolume = new SearchVolume(score, keyword);
        HttpStatus status = searchAlgorithm.hasNext() ? GATEWAY_TIMEOUT : OK;

        return status(status).body(searchVolume);
    }

}