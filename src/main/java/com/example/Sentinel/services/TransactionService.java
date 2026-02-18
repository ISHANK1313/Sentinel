package com.example.Sentinel.services;

import com.example.Sentinel.dto.MoneyTransferDto;
import com.example.Sentinel.dto.RiskAssessmentDto;
import com.example.Sentinel.dto.TransactionDto;
import com.example.Sentinel.entity.Transaction;

import com.example.Sentinel.entity.Users;
import com.example.Sentinel.repo.TransactionRepo;
import com.example.Sentinel.repo.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Service
public class TransactionService {
    @Autowired
    private UsersRepo usersRepo;
    @Autowired
    private TransactionRepo transactionRepo;
    @Autowired
    private RiskScoringService riskScoringService;
    @Autowired
    private RedisTemplate<String,String>redisTemplate;
    @Transactional
    public RiskAssessmentDto storeTransaction(MoneyTransferDto moneyTransferDto) {
        if (usersRepo.existsById(moneyTransferDto.getUserId()) && usersRepo.existsById(moneyTransferDto.getMerchantId())) {
            Transaction transaction= new Transaction();
            initialiseTransaction(transaction,moneyTransferDto);
            transaction=transactionRepo.save(transaction);
            storeInRedisForVelocity(transaction);
            RiskAssessmentDto dto=riskScoringService.RiskEngine(getAll30DaysTransaction(moneyTransferDto.getUserId()),transaction,redisTemplate);
            storeInRedisForHistory(transaction);
            transactionRepo.save(transaction);
          return dto;
        }
        return null;
    }
    public TransactionDto getTransactionDetails(Long transactionId){

        if(!transactionRepo.existsById(transactionId)){
            return null;
        }
            TransactionDto dto= new TransactionDto();
            Transaction t = transactionRepo.findById(transactionId).get();
            dto.setAmount(t.getAmount());
            dto.setCrossBorder(t.isCrossBorder());
            dto.setStatus(t.getStatus());
            dto.setTimeOfTransaction(t.getTimeOfTransaction());
            dto.setMerchantId(t.getMerchantId());
            dto.setUserId(t.getUsers().getUserId());
            dto.setDeviceFingerPrint(t.getDeviceFingerPrint());
            dto.setUserLocation(t.getUserLocation());
            dto.setMerchantCategoryCode(t.getMerchantCategoryCode());
            dto.setTransactionId(transactionId);
            return dto;

    }
  public List<TransactionDto> getTop10RecentTransactionFromUser(Long userId){
        List<Transaction> transactionList=transactionRepo.findTop10ByUsers_UserIdOrderByTimeOfTransactionDesc(userId);
        if(transactionList.isEmpty()){
            return null;
        }

        return wrapAround(transactionList);
  }

   private List<TransactionDto> wrapAround(List<Transaction> transactionList){
       List<TransactionDto> dtoList= new ArrayList<>();
       for(int i=0;i<transactionList.size();i++){
           TransactionDto dto=new TransactionDto();
           dto.setTransactionId(transactionList.get(i).getTransactionId());
           dto.setUserId(transactionList.get(i).getUsers().getUserId());
           dto.setAmount(transactionList.get(i).getAmount());
           dto.setStatus(transactionList.get(i).getStatus());
           dto.setUserLocation(transactionList.get(i).getUserLocation());
           dto.setTimeOfTransaction(transactionList.get(i).getTimeOfTransaction());
           dto.setMerchantCategoryCode(transactionList.get(i).getMerchantCategoryCode());
           dto.setMerchantId(transactionList.get(i).getMerchantId());
           dto.setCrossBorder(transactionList.get(i).isCrossBorder());
           dto.setDeviceFingerPrint(transactionList.get(i).getDeviceFingerPrint());
           dtoList.add(dto);
       }
       return dtoList;
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
        String cacheKey="history:"+"users:"+userId;
        long now=System.currentTimeMillis();
        long thirtyDaysAgo=now-Duration.ofDays(30).toMillis();
        Set<String> txnIds=redisTemplate.opsForZSet().rangeByScore(cacheKey,thirtyDaysAgo,now);
        if(txnIds==null||txnIds.isEmpty()){
            return transactionRepo.findByUsers_UserIdAndTimeOfTransactionAfter(
                    userId,LocalDateTime.now().minusDays(30))
                    .orElse(new ArrayList<>());
        }
        List<Long> ids = txnIds.stream()
                .map(Long::parseLong)
                .toList();
     return transactionRepo.findAllById(ids);
    }
   public List<TransactionDto> getAll30DaysTransactionDto(Long userId){
        List<Transaction> list=getAll30DaysTransaction(userId);
       if(list.isEmpty()){
           return null;
       }
        return wrapAround(list);
   }
    public void storeInRedisForVelocity(Transaction transaction) {

        String userId = transaction.getUsers().getUserId().toString();

        String key = "users:" + userId + ":velocity";
        long now=System.currentTimeMillis();
        long cutoff=now-Duration.ofHours(24).toMillis();
        redisTemplate.opsForZSet().removeRangeByScore(key,0,cutoff);
        redisTemplate.opsForZSet().add(key,transaction.getTransactionId().toString(),now);


    }

    public void storeInRedisForHistory(Transaction transaction){
        String key="history:users:"+transaction.getUsers().getUserId();
        long now=System.currentTimeMillis();
        long thirtyDaysAgo=now-Duration.ofDays(30).toMillis();
        redisTemplate.opsForZSet().removeRangeByScore(key,0,thirtyDaysAgo);
        redisTemplate.opsForZSet().add(key,transaction.getTransactionId().toString(),now);
    }


}
