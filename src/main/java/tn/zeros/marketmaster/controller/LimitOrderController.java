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
    @PostMapping("/add/{id}")
    public LimitOrderDTO addOrder(@PathVariable("id") Long id, @RequestBody LimitOrderDTO limitOrderDTO) {
        return limitOrderService.AddLimitOrder(id,limitOrderDTO);
    }
}
