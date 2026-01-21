package com.example.Sentinel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class MoneyTransferDto {
    @NotNull(message="amount can not be null")
    @NotBlank(message ="amount can not be blank")
    private Double amount;
    @NotNull(message="Location can not be null")
    @NotBlank(message ="Location can not be blank")
    private String locationOfUser;
    @NotNull(message="time can not be null")
    @NotBlank(message ="time can not be blank")
    private LocalDateTime timeOfPayment;
    @NotNull(message="merchant_ID can not be null")
    @NotBlank(message ="merchant_ID can not be blank")
    private String merchantId;
    @NotNull(message="user_ID can not be null")
    @NotBlank(message ="user_ID can not be blank")
    private String userId;
}
