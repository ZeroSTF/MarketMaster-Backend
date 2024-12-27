package tn.zeros.marketmaster.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tn.zeros.marketmaster.dto.TransactionDTO;

import tn.zeros.marketmaster.service.TransactionService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tran")
@Slf4j
public class TransactionController {
    private final TransactionService transactionService;
    @PostMapping("/ajout/{username}")
    public TransactionDTO ajout(@PathVariable("username") String username, @RequestBody TransactionDTO t){
        log.info("Received transaction for user {}: {}", username, t);
        return transactionService.addTransaction(username,t);
    }

    @GetMapping("/max/{username}/{symbol}")
    public TransactionDTO max(@PathVariable("username") String username, @PathVariable("symbol") String symbol){

        return transactionService.findMaxQuantity(symbol,username);
    }
}
