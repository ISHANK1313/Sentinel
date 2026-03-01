package com.example.Sentinel.services;

import com.example.Sentinel.dto.RiskAssessmentDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class BroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    public BroadcastService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendRiskUpdate(RiskAssessmentDto dto) {
        messagingTemplate.convertAndSend("/dashboard/alerts", dto);
    }
}