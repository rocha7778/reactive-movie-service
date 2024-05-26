package com.rocha.aws.app.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    private String id;
    private String movieInfoId;
    private String comment;
    private Double rating;
}
