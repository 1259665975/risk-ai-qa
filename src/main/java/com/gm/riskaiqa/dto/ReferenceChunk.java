<<<<<<< HEAD
package com.gm.riskaiqa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * A retrieved knowledge snippet used to ground the answer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceChunk implements Serializable {

    private String content;
    private String source;
    private Double score;
}
=======
package com.gm.riskaiqa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * A retrieved knowledge snippet used to ground the answer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceChunk implements Serializable {

    private String content;
    private String source;
    private Double score;
}
>>>>>>> 7998cf43f5debde367904ed821ec8539275331cc
