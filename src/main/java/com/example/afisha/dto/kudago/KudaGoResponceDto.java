package com.example.afisha.dto.kudago;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class KudaGoResponceDto {

    private Integer count;

    private String next;

    private List<KudaGoEventDto> results;

}
