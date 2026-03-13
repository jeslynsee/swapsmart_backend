package com.group.SwapSmart.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "profiles")
public class Profile {

    /* Represents our profiles table, where we will store user info such as id, first name,
       last name, and username. This will help us later when we need to display reviews with 
       different users' names, even if we are doing user auth through Supabase Auth.
     */

    // Primary key — matches Supabase auth.users UUID
    @Id
    private String id;

    // First name
    @Column(name = "first_name", nullable = false)
    private String firstName;

    // Last name
    @Column(name = "last_name", nullable = false)
    private String lastName;

    // Username
    @Column(nullable = false, unique = true)
    private String username;

    // Constructors
    public Profile() {
    }

    public Profile(String id, String firstName, String lastName, String username) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}