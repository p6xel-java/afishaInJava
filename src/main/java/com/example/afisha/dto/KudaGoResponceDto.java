package com.example.afisha.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class KudaGoResponceDto {

    private List<KudaGoEventDto> results;

}
