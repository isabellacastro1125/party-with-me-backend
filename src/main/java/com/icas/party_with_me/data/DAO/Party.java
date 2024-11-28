package com.icas.party_with_me.data.DAO;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.ArrayList;

@Entity
@Table(name = "parties")
public class Party {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime time;

    @Column(nullable = false)
    private String location;

    private String theme;

    // Many parties can be created by one user (many-to-one relationship)
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;  // User who created the party
    
    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // Forward reference
    private List<Invitation> invitations = new ArrayList<>();


    @OneToMany(mappedBy = "party", cascade = {}, orphanRemoval = true)
    @JsonManagedReference // Forward reference
    private List<Item> items = new ArrayList<>();

    // Default constructor
    public Party() {}

    // Parameterized constructor
    public Party(String title, String description, LocalDate date, LocalTime time, String location, String theme, User createdBy) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.location = location;
        this.theme = theme;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
    
    public List<Item> getItems() {
        return items;
    }
    
    public void setItems(List<Item> items) {
        this.items = items;
    }
    
    public List<Invitation> getInvitations() { // Updated to match the field name
        return invitations;
    }
    
    public void setInvitations(List<Invitation> invitations) { // Updated to match the field name
        this.invitations = invitations;
    }

    @Override
    public String toString() {
        return "Party{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                ", time=" + time +
                ", location='" + location + '\'' +
                ", theme='" + theme + '\'' +
                ", createdBy=" + createdBy +
                '}';
    }
}
