package tn.zeros.marketmaster.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tn.zeros.marketmaster.dto.LimitOrderDTO;
import tn.zeros.marketmaster.service.LimitOrderService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
@Slf4j
public class LimitOrderController {
    private final LimitOrderService limitOrderService;
    @PostMapping("/add/{username}")
    public LimitOrderDTO addOrder(@PathVariable("username") String username, @RequestBody LimitOrderDTO limitOrderDTO) {
        return limitOrderService.addLimitOrder(username,limitOrderDTO);
    }
    @DeleteMapping("/delete/{username}")
    public void deleteOrder(@PathVariable("username") String username, @RequestBody LimitOrderDTO limitOrderDTO) {
        limitOrderService.deleteLimitOrder(username,limitOrderDTO);
    }
}
