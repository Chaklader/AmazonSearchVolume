package com.sellics.challenge.calculation;


import lombok.Getter;
import lombok.ToString;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.removeEnd;


/*
 * It calculates the score based on how many of the sub-prefixes of a
 * search term, will result in autocompletion containing the search term
 * itself.
 * */

/**
 * @author Chaklader on 2020-02-07
 */
@ToString(exclude = {"settings"})
public class SearchVolumeCountAlgorithm implements SearchVolumeIterator {

    public static final int PERCENT = 100;

    @Getter
    private Settings settings;

    private String subPrefix;
    private String keyword;
    private String trimmedKeyword;

    private float score = 0;
    private int count;

    public SearchVolumeCountAlgorithm(Settings settings, String keyword) {

        this.keyword = keyword;
        this.settings = settings;

        this.trimmedKeyword = removeAllStopSufixes(keyword.toLowerCase());

        count = calculateTotalIterations(trimmedKeyword);
        subPrefix = trimmedKeyword;
    }

    @Override
    public boolean hasNext() {
        return subPrefix.length() > 0;
    }

    @Override
    public Integer next() {

        Set<String> matches = settings.findMatches(subPrefix);

        if (matches.contains(trimmedKeyword)) {

            score += getMatchesWeight(matches);

            subPrefix = subPrefix.substring(0, subPrefix.length() - 1);
            subPrefix = removeAllStopSufixes(subPrefix);
        } else {
            subPrefix = "";
        }

        return (int) ((score * PERCENT) / count);
    }

    public float getMatchesWeight(Set<String> mathes) {
        return ((float) mathes.size()) / settings.getMaxResultSetSize();
    }

    public int calculateTotalIterations(String input) {

        int total = 0;
        input = removeAllStopSufixes(input);

        while (input.length() > 0) {

            total++;

            input = input.substring(0, input.length() - 1);
            input = removeAllStopSufixes(input);
        }

        return total;
    }

    public String removeAllStopSufixes(String input) {

        String result = input;
        boolean hasChanged;

        do {

            hasChanged = false;

            for (String stopSufix : settings.getSuffixStopWords()) {

                if (result.endsWith(stopSufix)) {
                    result = removeEnd(result, stopSufix);
                    hasChanged = true;
                }
            }
        }

        while (hasChanged);
        return result;
    }
}
