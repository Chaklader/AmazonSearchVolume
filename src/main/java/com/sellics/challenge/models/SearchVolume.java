package com.sellics.challenge.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author Chaklader on 2020-02-07
 */
@Getter
@Setter
@Accessors(chain = true)
public class SearchVolume {

    private int score;

    @JsonProperty("Keyword")
    private String keyword;

    public SearchVolume(int score, String keyword) {
        this.score = score;
        this.keyword = keyword;
    }
}
