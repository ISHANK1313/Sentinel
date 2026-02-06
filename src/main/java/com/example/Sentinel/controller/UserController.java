package com.example.Sentinel.controller;

import com.example.Sentinel.dto.UsersDetailDto;
import com.example.Sentinel.entity.Users;
import com.example.Sentinel.repo.UsersRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UsersRepo usersRepo;
    @PostMapping("/add")
    public ResponseEntity<?> addUser(@RequestBody @Valid UsersDetailDto usersDetailDto) {
        try {
            if (usersRepo.existsByEmail(usersDetailDto.getEmail())) {
                return ResponseEntity.status(HttpStatus.CREATED).body("User already existed");
            }
            Users users = new Users();
            users.setEmail(usersDetailDto.getEmail());
            users.setName(usersDetailDto.getName());
            users.setHomeLocation(usersDetailDto.getHomeLocation());
            users.setCreatedAt(LocalDateTime.now());
            usersRepo.save(users);
            return ResponseEntity.status(HttpStatus.CREATED).body("User created...");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("unable to create user currently please try again");
        }
    }
    @GetMapping("/userdetails")
    public ResponseEntity<?> getUserDetail(@RequestParam Long user_id){
        if(usersRepo.existsById(user_id)){
            return ResponseEntity.ok().body(usersRepo.findById(user_id));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user does not exists");

    }

    @DeleteMapping("/clearUser")
    public ResponseEntity<?> deleteUser(@RequestParam Long user_id){
        try {
            if (usersRepo.existsById(user_id)) {
                usersRepo.delete(usersRepo.findById(user_id).get());
                return ResponseEntity.ok().body("User Deleted successfully");
            }
            else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with this id does not exists");
            }
        } catch (Exception e) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
        return null;
    }



}
