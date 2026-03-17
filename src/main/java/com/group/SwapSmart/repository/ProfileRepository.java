package com.group.SwapSmart.repository;
import com.group.SwapSmart.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, String> {
    // need this file to communicate with profiles table in Supabase
}