package com.sellics.test.calculation;


import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.List;
import java.util.Set;
import java.util.function.Function;


/**
 * @author Chaklader on 2020-02-07
 */
@Value
@Wither
@Getter
@Builder
public class Settings {

    private Function<String, Set<String>> matchFinder;
    private int maxResultSetSize;
    private List<String> suffixStopWords;

    public Set<String> findMatches(String keyword) {
        return matchFinder.apply(keyword);
    }
}
