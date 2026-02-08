package com.example.Sentinel.controller;

import com.example.Sentinel.dto.MoneyTransferDto;
import com.example.Sentinel.dto.RiskAssessmentDto;
import com.example.Sentinel.dto.TransactionDto;
import com.example.Sentinel.services.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transaction")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;
    @PostMapping("/check")
    public ResponseEntity<?>postTransaction(@RequestBody @Valid MoneyTransferDto moneyTransferDto){
        try{
            RiskAssessmentDto dto=transactionService.storeTransaction(moneyTransferDto);
            if(dto==null){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("there was an error while submitting the transaction");
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (Exception e) {
           return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

     }
     @GetMapping("/details")
     public ResponseEntity<?> getTransactionDetails(@RequestParam @Valid Long transactionId){
        TransactionDto dto=transactionService.getTransactionDetails(transactionId);
        if(dto.equals(null)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("no such transaction id exits");
        }
        return ResponseEntity.ok().body(dto);
     }

     @GetMapping("/top10ForUser")
    public ResponseEntity<?> Top10TransactionForUser(@RequestParam @Valid Long userId){
        List<TransactionDto> dtoList=transactionService.getTop10RecentTransactionFromUser(userId);
        if(dtoList.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("no such user exits");
        }
        return ResponseEntity.ok().body(dtoList);
     }
     @GetMapping("/Last30ForUser")
     public ResponseEntity<?> Last30DaysTransactionForUser(@RequestParam @Valid Long userId){
        List<TransactionDto> dtoList=transactionService.getAll30DaysTransactionDto(userId);
        if(dtoList.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("no such user exits");
        }
        return ResponseEntity.ok().body(dtoList);
     }

}
