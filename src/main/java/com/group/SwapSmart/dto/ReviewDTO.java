package com.group.SwapSmart.dto;

import java.time.LocalDateTime;

public class ReviewDTO {

    private Long id;
    private String displayName;
    private String username;
    private String barcode;
    private Integer rating;
    private String comment;
    private LocalDateTime timeCreated;

    // Constructor
    public ReviewDTO(Long id, String displayName, String username, String barcode, Integer rating, String comment, LocalDateTime timeCreated) {
        this.id = id;
        this.displayName = displayName;
        this.username = username;
        this.barcode = barcode;
        this.rating = rating;
        this.comment = comment;
        this.timeCreated = timeCreated;
    }

    // Getters
    public Long getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getUsername() { return username; }
    public String getBarcode() { return barcode; }
    public Integer getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDateTime getTimeCreated() { return timeCreated; }
}