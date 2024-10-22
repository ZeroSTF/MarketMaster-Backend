package tn.zeros.marketmaster.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tn.zeros.marketmaster.dto.TransactionDTO;

import tn.zeros.marketmaster.service.TransactionService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tran")
@Slf4j
public class TransactionController {
    private final TransactionService transactionService;
    @PostMapping("/ajout/{userName}")
    public TransactionDTO ajout(@PathVariable("userName") String userName, @RequestBody TransactionDTO t){
        return transactionService.addTransaction(userName,t);
    }
}
