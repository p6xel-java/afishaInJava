package com.example.afisha.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String externalId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String venue;

    private BigDecimal price;

    private LocalDateTime startDateTime;

    public Event(){

    }

    public Event(Long id, String externalId, String title,
                 String description, String venue,
                 BigDecimal price, LocalDateTime startDateTime) {
        this.id = id;
        this.externalId = externalId;
        this.title = title;
        this.description = description;
        this.venue = venue;
        this.price = price;
        this.startDateTime = startDateTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }
}
