package com.example.Sentinel.repo;

import com.example.Sentinel.entity.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiskAssessmentRepo extends JpaRepository<RiskAssessment,Long> {
    Optional<RiskAssessment> findByTransaction_TransactionId(Long aLong);
}
