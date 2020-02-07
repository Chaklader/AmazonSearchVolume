package com.sellics.test.calculation;
/*
 * @author aleksandartrposki@gmail.com
 * @since 14.07.19
 *
 *
 */

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

public class SearchVolumeCountAlgorithmTest {
    @Test
    public void getMaximumItterationCount_takesConsecutiveStopWOrdsIntoAccount() {
        Settings settings = Settings.builder()
                .suffixStopWords(asList("1", "2", "3"))
                .build();
        String keyword = "1abc121abc3a3abc1";
        SearchVolumeCountAlgorithm searchVolumeCountAlgorithm = new SearchVolumeCountAlgorithm(settings, keyword);
        int result = searchVolumeCountAlgorithm.calculateTotalIterations(keyword);
        Assert.assertEquals(10, result);
    }

    @Test
    public void getMaximumItterationCount_returnsZeroForEmptyString() {
        Settings settings = Settings.builder()
                .suffixStopWords(asList("1", "2", "3"))
                .build();
        String keyword = "";
        SearchVolumeCountAlgorithm searchVolumeCountAlgorithm = new SearchVolumeCountAlgorithm(settings, keyword);
        int result = searchVolumeCountAlgorithm.calculateTotalIterations(keyword);
        Assert.assertEquals(0, result);
    }

    @Test
    public void getMaximumItterationCount_returnsStrLengthForSufixless() {
        Settings settings = Settings.builder()
                .suffixStopWords(asList("1", "2", "3"))
                .build();
        String keyword = "abcde";
        SearchVolumeCountAlgorithm searchVolumeCountAlgorithm = new SearchVolumeCountAlgorithm(settings, keyword);
        int result = searchVolumeCountAlgorithm.calculateTotalIterations(keyword);
        Assert.assertEquals(keyword.length(), result);
    }

    @Test
    public void getMaximumItterationCount_returnsStrLengthForEmptySufixArray() {
        Settings settings = Settings.builder()
                .suffixStopWords(new ArrayList<>())
                .build();
        String keyword = "abcde";
        SearchVolumeCountAlgorithm searchVolumeCountAlgorithm = new SearchVolumeCountAlgorithm(settings, keyword);
        int result = searchVolumeCountAlgorithm.calculateTotalIterations(keyword);
        Assert.assertEquals(keyword.length(), result);
    }

    @Test
    public void getMaximumItterationCount_ReturnsZeroForStopWOrdsOnlyKeyword() {
        Settings settings = Settings.builder()
                .suffixStopWords(asList("1", "2", "3"))
                .build();
        String keyword = "11231";
        SearchVolumeCountAlgorithm searchVolumeCountAlgorithm = new SearchVolumeCountAlgorithm(settings, keyword);
        int result = searchVolumeCountAlgorithm.calculateTotalIterations(keyword);
        Assert.assertEquals(0, result);
    }

    @Test
    public void removeAllSufixes_ReturnsCorrectWordForDuplicateStopSuffixesInMixedOrder() {
        Settings settings = Settings.builder()
                .suffixStopWords(asList("1", "2", "3"))
                .build();
        String keyword = "1abc121";
        SearchVolumeCountAlgorithm searchVolumeCountAlgorithm = new SearchVolumeCountAlgorithm(settings, keyword);

        String result = searchVolumeCountAlgorithm.removeAllStopSufixes(keyword);
        Assert.assertEquals("1abc", result);
    }

    @Test
    public void removeAllSufixes_DoesntChange_sufixlessWord() {
        Settings settings = Settings.builder()
                .suffixStopWords(asList("1", "2", "3"))
                .build();
        String keyword = "1abc";
        SearchVolumeCountAlgorithm searchVolumeCountAlgorithm = new SearchVolumeCountAlgorithm(settings, keyword);

        String result = searchVolumeCountAlgorithm.removeAllStopSufixes(keyword);
        Assert.assertEquals("1abc", result);
    }

    @Test
    public void removeAllSufixes_DoesntChangeWord_WhenSufixArrayIsEmpty() {
        Settings settings = Settings.builder()
                .suffixStopWords(asList("1", "2", "3"))
                .build();
        String keyword = "1abc";
        SearchVolumeCountAlgorithm searchVolumeCountAlgorithm = new SearchVolumeCountAlgorithm(settings, keyword);

        String result = searchVolumeCountAlgorithm.removeAllStopSufixes(keyword);
        Assert.assertEquals("1abc", result);
    }


    @Test
    public void whenKeywordMatchesInAllIterations_andResultsAreFull_scoreIs100() {
        String keyword = "1abc1asd23";
        String sufixlessKeyword = "1abc1asd";
        Settings settings = Settings.builder()
                .suffixStopWords(asList("1", "2", "3"))
                .maxResultSetSize(3)
                .matchFinder((term) -> asList(sufixlessKeyword, term + "aaaaa", term + "bbbbb").stream().collect(toSet()))
                .build();
        SearchVolumeCountAlgorithm searchVolumeCountAlgorithm = new SearchVolumeCountAlgorithm(settings, keyword);


        int result = 0;
        while (searchVolumeCountAlgorithm.hasNext()) {
            result = searchVolumeCountAlgorithm.next();
        }
        Assert.assertEquals(100, result);
    }

    @Test
    public void whenKeywordMatchesInHalfOfAllIterations_andResultsAreFull_scoreIs50() {
        String keyword = "a1b1";
        Settings settings = Settings.builder()
                .suffixStopWords(asList("1", "2", "3"))
                .maxResultSetSize(3)
                .matchFinder((term) -> asList(term, term + "aaaaa", term + "bbbbb").stream().collect(toSet()))
                .build();
        SearchVolumeCountAlgorithm searchVolumeCountAlgorithm = new SearchVolumeCountAlgorithm(settings, keyword);


        int result = 0;
        while (searchVolumeCountAlgorithm.hasNext()) {
            result = searchVolumeCountAlgorithm.next();
        }
        Assert.assertEquals(50, result);
    }

    @Test
    public void whenKeywordMatchesInAllIterations_andResultsAreHalfEmptyFull_scoreIs50() {
        String keyword = "a1b1";
        Settings settings = Settings.builder()
                .suffixStopWords(asList("1", "2", "3"))
                .maxResultSetSize(6)
                .matchFinder((term) -> asList("a1b", term + "aaaaa", term + "bbbbb").stream().collect(toSet()))
                .build();
        SearchVolumeCountAlgorithm searchVolumeCountAlgorithm = new SearchVolumeCountAlgorithm(settings, keyword);


        int result = 0;
        while (searchVolumeCountAlgorithm.hasNext()) {
            result = searchVolumeCountAlgorithm.next();
        }
        Assert.assertEquals(50, result);
    }


    @Test
    public void whenAmazonReturnsNoMatches_scoreIs0() {
        String keyword = "a1b1";
        Settings settings = Settings.builder()
                .suffixStopWords(asList("1", "2", "3"))
                .maxResultSetSize(6)
                .matchFinder((term) -> new HashSet<>())
                .build();
        SearchVolumeCountAlgorithm searchVolumeCountAlgorithm = new SearchVolumeCountAlgorithm(settings, keyword);


        int result = 0;
        while (searchVolumeCountAlgorithm.hasNext()) {
            result = searchVolumeCountAlgorithm.next();
        }
        Assert.assertEquals(0, result);
    }
}
