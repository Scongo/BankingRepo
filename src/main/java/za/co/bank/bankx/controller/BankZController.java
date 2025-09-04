package za.co.bank.bankx.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.bank.bankx.domain.entities.TransactionRecord;
import za.co.bank.bankx.dto.BankzDTO;
import za.co.bank.bankx.service.BankingService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/bankz")
public class BankZController {
    private final BankingService bankingService;

    @Autowired
    public BankZController(BankingService bankingService) { this.bankingService = bankingService; }


    // Single immediate transaction
    @PostMapping("/transaction")
    public ResponseEntity<TransactionRecord> processTransaction(@RequestParam(required = false) String fromAccountId,
                                                                @RequestParam(required = false) String toAccountId,
                                                                @RequestParam BigDecimal amount) {
        TransactionRecord transactionRecord = bankingService.processBankZTransaction(fromAccountId, toAccountId, amount, "BANK_Z_IMMEDIATE");
        return ResponseEntity.ok(transactionRecord);
    }


    //send a single immediate transaction or a list of transactions which should be processed immediately
    @PostMapping("/transactions/batch")
    public ResponseEntity<List<TransactionRecord>> processBatch(@RequestBody List<BankzDTO> bankzDTOS) {
        List<TransactionRecord> results = new ArrayList<>();
        for (BankzDTO bankzDTO : bankzDTOS) {
            TransactionRecord transactionRecord = bankingService.processBankZTransaction(bankzDTO.getFromAccountId(), bankzDTO.getToAccountId(), bankzDTO.getAmount(), "BANK_Z_BATCH");
            results.add(transactionRecord);
        }
        return ResponseEntity.ok(results);
    }
}