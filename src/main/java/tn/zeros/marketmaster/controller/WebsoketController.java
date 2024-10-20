package tn.zeros.marketmaster.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import tn.zeros.marketmaster.dto.AssetDailyDto;
import tn.zeros.marketmaster.service.YfinaceService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebsoketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final YfinaceService yfinaceService;

    @MessageMapping("/fetchStockData")
    public void fetchStockData() {
        List<AssetDailyDto> dailyDtos = yfinaceService.dailyDtos();
        messagingTemplate.convertAndSend("/topic/market", dailyDtos);
    }

}
