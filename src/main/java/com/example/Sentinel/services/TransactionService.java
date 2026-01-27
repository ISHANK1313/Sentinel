package com.example.Sentinel.services;

import com.example.Sentinel.dto.MoneyTransferDto;
import com.example.Sentinel.entity.Transaction;

import com.example.Sentinel.entity.Users;
import com.example.Sentinel.repo.TransactionReo;
import com.example.Sentinel.repo.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
public class TransactionService {
    @Autowired
    private UsersRepo usersRepo;
    @Autowired
    private TransactionReo transactionReo;
    @Transactional
    public boolean storeTransaction(MoneyTransferDto moneyTransferDto) {
        if (usersRepo.existsById(moneyTransferDto.getUserId()) && usersRepo.existsById(moneyTransferDto.getMerchantId())) {
            Transaction t = new Transaction();
            t.setAmount(moneyTransferDto.getAmount());
            t.setTimeOfTransaction(moneyTransferDto.getTimeOfPayment());
            t.setStatus("UNFLAGGED");
            t.setMerchantId(moneyTransferDto.getMerchantId());
            t.setUserLocation(moneyTransferDto.getLocationOfUser());
            Optional<Users> u=usersRepo.findById(moneyTransferDto.getUserId());
            t.setUsers(u.get());
            transactionReo.save(t);
          return true;
        }
        return false;
    }



}
