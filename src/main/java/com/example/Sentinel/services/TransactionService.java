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
            storeInRedis(transaction);
            RiskAssessmentDto dto=riskScoringService.RiskEngine(getAll30DaysTransaction(moneyTransferDto.getUserId()),transaction,redisTemplate);
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
       Optional<List<Transaction>> transactionList= transactionRepo.findByUsers_UserIdAndTimeOfTransactionAfter(userId,LocalDateTime.now().minusDays(30));
     return transactionList.orElse(new ArrayList<>());
    }
   public List<TransactionDto> getAll30DaysTransactionDto(Long userId){
        List<Transaction> list=getAll30DaysTransaction(userId);
       if(list.isEmpty()){
           return null;
       }
        return wrapAround(list);
   }
    public void storeInRedis(Transaction transaction) {

        String userId = transaction.getUsers().getUserId().toString();

        String key5Min = "users:" + userId + ":velocity:5min";
        String key1Hr = "users:" + userId + ":velocity:1hr";
        String key24Hr = "users:" + userId + ":velocity:24hr";

        Long count5Min = redisTemplate.opsForValue().increment(key5Min);
        Long count1Hr=redisTemplate.opsForValue().increment(key1Hr);
        Long count24Hr=redisTemplate.opsForValue().increment(key24Hr);

        if (count5Min == 1) {
            redisTemplate.expire(key5Min, Duration.ofMinutes(5));
        }
        if (count1Hr == 1) {
            redisTemplate.expire(key1Hr, Duration.ofHours(1));
        }
        if (count24Hr == 1) {
            redisTemplate.expire(key24Hr, Duration.ofDays(1));
        }
    }


}
