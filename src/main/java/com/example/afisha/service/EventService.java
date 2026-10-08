package com.example.afisha.service;

import com.example.afisha.dto.EventRequestDto;
import com.example.afisha.dto.EventResponseDto;

import java.util.List;

public interface EventService {

    void syncEvents();

    List<EventResponseDto> getAllEvents();

    EventResponseDto getEventById(Long id);

    EventResponseDto createEvent(EventRequestDto requestDto);

}
