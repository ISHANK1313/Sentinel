package com.example.Sentinel.repo;

import com.example.Sentinel.entity.RiskAssessment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiskAssessmentRepo extends JpaRepository<RiskAssessment,Long> {
    @EntityGraph(attributePaths = {"transaction", "transaction.users"})
    Optional<RiskAssessment> findByTransaction_TransactionId(Long aLong);

    @EntityGraph(attributePaths = {"transaction", "transaction.users"})
    List<RiskAssessment> findAllByOrderByIdDesc();
}
