package com.example.afisha.service;

import com.example.afisha.dto.KudaGoEventDto;

import java.util.List;

public interface EventFetcherService {

    List<KudaGoEventDto> fetchEventsFromExternalApi();
}
