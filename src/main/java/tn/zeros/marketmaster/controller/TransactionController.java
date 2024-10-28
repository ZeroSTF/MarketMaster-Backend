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
    @PostMapping("/ajout/{id}")
    public TransactionDTO ajout(@PathVariable("id") Long id, @RequestBody TransactionDTO t){
        return transactionService.ajoutUneTransaction(id,t);
    }
    @PostMapping("/stat/{id}/{s}")
    public List<TransactionDTO> getstat(@PathVariable("id") Long id,@PathVariable("s") String symbol){

        return transactionService.GetStatBySymbol(id,symbol);
        }
}
