package com.sellics.amazon.models;
/*
 * @author aleksandartrposki@gmail.com
 * @since 14.07.19
 *
 *
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class Estimation {
    private int score;
    @JsonProperty("Keyword")
    private String keyword;

    public Estimation(int score, String keyword) {
        this.score = score;
        this.keyword = keyword;
    }
}
