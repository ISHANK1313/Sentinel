package com.example.Sentinel.controller;

import com.example.Sentinel.dto.MoneyTransferDto;
import com.example.Sentinel.dto.RiskAssessmentDto;
import com.example.Sentinel.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transaction")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;
    @PostMapping("/check")
    public ResponseEntity<?>postTransaction(MoneyTransferDto moneyTransferDto){
        try{
            RiskAssessmentDto dto=transactionService.storeTransaction(moneyTransferDto);
            if(dto==null){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("there was an error while submitting the transaction");
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (Exception e) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
       return  null;
     }

}
