package com.example.afisha.dto.kudago;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class KudaGoEventDto {

    private Long id;

    private String title;

    private String description;

    private String price;

    @JsonProperty("dates")
    private List<DateTime> dates;

    @Getter
    @Setter
    public static class DateTime {
        private Long start;
        private Long end;
    }
}
