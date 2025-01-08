package tn.zeros.marketmaster.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.zeros.marketmaster.dto.TransactionDTO;
import tn.zeros.marketmaster.entity.Option;
import tn.zeros.marketmaster.service.OptionService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/option")
public class OptionController {
    private final OptionService optionService;
    @PostMapping("/buyoption/{username}")
    public ResponseEntity<Option> buyOption(@RequestBody Option option,@PathVariable String username){
        Option option1 = optionService.buyOption(username, option);
        return ResponseEntity.ok(option1);
    }
    @PostMapping("/preview")
    public double getPreview(@RequestBody Option option){
        return optionService.getOptionPrime(option);
    }
    @PostMapping("/applyoption/{username}")
    public ResponseEntity<TransactionDTO> apply(@RequestBody Option option,@PathVariable String username){
        TransactionDTO transactionDTO = optionService.applyOption(option,username);
        return ResponseEntity.ok(transactionDTO);
    }
}
