package com.group.SwapSmart.repository;

import com.group.SwapSmart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    // we will define custom queries here
}