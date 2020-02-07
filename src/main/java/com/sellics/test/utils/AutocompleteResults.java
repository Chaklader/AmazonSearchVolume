package com.sellics.test.utils;


import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Chaklader on 2020-02-07
 */
public class AutocompleteResults {


    List<JsonNode> response;
    Set<String> results;

    public AutocompleteResults(List<JsonNode> response) {
        this.response = response;
    }

    public String getSearchTerm() {
        return response.get(0).asText();
    }

    public Set<String> getMatches() {

        if (results != null) {
            return results;
        }

        results = new HashSet<>();
        response.get(1)
                .iterator()
                .forEachRemaining(match -> results.add(match.asText()));
        return results;
    }
}
