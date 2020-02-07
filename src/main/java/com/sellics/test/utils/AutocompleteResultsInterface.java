package com.sellics.test.utils;


/**
 * Example request
 * http://completion.amazon.com/search/complete?search-alias=aps&mkt=1&q=apple%20watch
 */

/**
 * @author Chaklader on 2020-02-07
 */
public interface AutocompleteResultsInterface {

    AutocompleteResults autocomplete(String searchTerm, String market, String department);
}
