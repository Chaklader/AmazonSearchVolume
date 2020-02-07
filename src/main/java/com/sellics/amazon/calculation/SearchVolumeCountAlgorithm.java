package com.sellics.amazon.calculation;


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
    private AlgorithmSettings settings;

    private String subPrefix;
    private String keyword;
    private String trimedKeyword;

    private float score = 0;
    private int totalIterations = 0;

    public SearchVolumeCountAlgorithm(AlgorithmSettings settings, String keyword) {

        this.keyword = keyword;
        this.settings = settings;

        this.trimedKeyword = removeAllStopSufixes(keyword.toLowerCase());

        totalIterations = calculateTotalIterations(trimedKeyword);
        subPrefix = trimedKeyword;
    }

    @Override
    public boolean hasNext() {
        return subPrefix.length() > 0;
    }

    @Override
    public Integer next() {

        Set<String> mathes = settings.findMatches(subPrefix);

        if (mathes.contains(trimedKeyword)) {

            score += getMatchesWeight(mathes);

            subPrefix = subPrefix.substring(0, subPrefix.length() - 1);
            subPrefix = removeAllStopSufixes(subPrefix);
        } else {
            subPrefix = "";
        }

        return (int) ((score * PERCENT) / totalIterations);
    }

    public float getMatchesWeight(Set<String> mathes) {
        return ((float) mathes.size()) / settings.getMaxResultSetSize();
    }

    public int calculateTotalIterations(String input) {

        int count = 0;
        input = removeAllStopSufixes(input);

        while (input.length() > 0) {

            count++;

            input = input.substring(0, input.length() - 1);
            input = removeAllStopSufixes(input);
        }

        return count;
    }

    public String removeAllStopSufixes(String input) {

        String res = input;
        boolean hasChanged;

        do {

            hasChanged = false;

            for (String stopSufix : settings.getSuffixStopWords()) {

                if (res.endsWith(stopSufix)) {
                    res = removeEnd(res, stopSufix);
                    hasChanged = true;
                }
            }
        }

        while (hasChanged);
        return res;
    }
}
