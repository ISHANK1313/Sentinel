package com.example.Sentinel.repo;

import com.example.Sentinel.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepo extends JpaRepository<Users,Long> {
    boolean existsById(Long aLong);
    boolean existsByEmail(String email);

}
