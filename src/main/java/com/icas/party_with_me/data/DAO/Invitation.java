package com.icas.party_with_me.data.DAO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "invitations")
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @JsonProperty("guest_name") // Maps "guest_name" from JSON to this field
    private String guest_name;

    @Column(nullable = false)
    @JsonProperty("guest_phone") // Maps "guest_phone" from JSON to this field
    private String guest_phone;

    private String rsvp;

    @ManyToOne
    @JoinColumn(name = "party_id", nullable = false)
    @JsonBackReference // Backward reference
    private Party party;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGuestName() { // Updated getter for guest_name
        return guest_name;
    }

    public void setGuestName(String guest_name) { // Updated setter for guest_name
        this.guest_name = guest_name;
    }

    public String getGuestPhone() { // Updated getter for guest_phone
        return guest_phone;
    }

    public void setGuestPhone(String guest_phone) { // Updated setter for guest_phone
        this.guest_phone = guest_phone;
    }

    public String getRsvp() {
        return rsvp;
    }

    public void setRsvp(String rsvp) {
        this.rsvp = rsvp;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }
}
