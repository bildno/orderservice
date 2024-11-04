package com.playdata.orderservice.user.repository;

import com.playdata.orderservice.user.dto.UserResDto;
import com.playdata.orderservice.user.entity.User;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;



public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);



}
