package com.example.Sentinel.repo;

import com.example.Sentinel.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepo extends JpaRepository<Transaction,Long> {
    Optional<List<Transaction>>findByUsersIdAndTimeOfTransactionAfter(Long userId, LocalDateTime thirtyDaysAgo);
    Optional<List<Transaction>> findByUsersId(Long aLong);
    Optional<List<Transaction>> findTop10ByUserIdOrderByTimeOfTransactionDesc(Long userId);
}
