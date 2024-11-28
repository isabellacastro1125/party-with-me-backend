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
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @JsonProperty("item_name") // Maps "item_name" from JSON to this field
    private String item_name;

    @Column(length = 1000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "invite_id")
    private Invitation broughtBy;

    @ManyToOne
    @JoinColumn(name = "party_id")
    @JsonBackReference // Backward reference
    private Party party;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemName() { // Updated to follow Java Bean conventions
        return item_name;
    }

    public void setItemName(String item_name) { // Updated to follow Java Bean conventions
        this.item_name = item_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Invitation getBroughtBy() {
        return broughtBy;
    }

    public void setBroughtBy(Invitation broughtBy) {
        this.broughtBy = broughtBy;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }
}
