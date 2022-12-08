package com.giraone.scs.resume.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Generated
public class MessageOut {

    private String requestId;
    // @JsonFormat(shape = JsonFormat.Shape.STRING) // Removed to test Mapping
    private LocalDateTime startTime;
    private MessageIn messageIn;
    private int calculatedValue1;
}
