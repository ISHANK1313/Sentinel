package com.example.Sentinel.services;

import com.example.Sentinel.dto.MoneyTransferDto;
import com.example.Sentinel.entity.Transaction;

import com.example.Sentinel.entity.Users;
import com.example.Sentinel.repo.TransactionRepo;
import com.example.Sentinel.repo.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
public class TransactionService {
    @Autowired
    private UsersRepo usersRepo;
    @Autowired
    private TransactionRepo transactionRepo;
    @Autowired
    private RiskScoringService riskScoringService;
    @Transactional
    public boolean storeTransaction(MoneyTransferDto moneyTransferDto) {
        if (usersRepo.existsById(moneyTransferDto.getUserId()) && usersRepo.existsById(moneyTransferDto.getMerchantId())) {
            Transaction transaction= new Transaction();
            initialiseTransaction(transaction,moneyTransferDto);
            riskScoringService.RiskEngine(getAll30DaysTransaction(moneyTransferDto.getUserId()),transaction);
            transactionRepo.save(transaction);
          return true;
        }
        return false;
    }
    private void initialiseTransaction( Transaction t,MoneyTransferDto dto){
        t.setAmount(dto.getAmount());
        t.setTimeOfTransaction(dto.getTimeOfPayment());
        t.setStatus("UNFLAGGED");
        t.setMerchantId(dto.getMerchantId());
        t.setUserLocation(dto.getLocationOfUser());
        Optional<Users> u=usersRepo.findById(dto.getUserId());
        t.setUsers(u.get());
        t.setMerchantCategoryCode(dto.getMerchantCategoryCode());
        t.setDeviceFingerPrint(dto.getDeviceFingerPrint());
        t.setCrossBorder(dto.isCrossBorder());
    }

    private List<Transaction> getAll30DaysTransaction(Long userId){
       Optional<List<Transaction>> transactionList= transactionRepo.findByUsers_UserIdAndTimeOfTransactionAfter(userId,LocalDateTime.now().minusDays(30));
        return transactionList.get();
    }



}
