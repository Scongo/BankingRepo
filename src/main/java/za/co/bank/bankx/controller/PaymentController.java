package za.co.bank.bankx.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.co.bank.bankx.domain.entities.TransactionRecord;
import za.co.bank.bankx.dto.PaymentRequest;
import za.co.bank.bankx.service.BankingService;

@RestController
@RequestMapping("/api/payments")
@Validated
public class PaymentController {
    private final BankingService bankingService;


    @Autowired
    public PaymentController(BankingService bankingService) {
        this.bankingService = bankingService;
    }


    @PostMapping("/transfer")
    public ResponseEntity<TransactionRecord> transfer(@Valid @RequestBody PaymentRequest request) {
        TransactionRecord transactionRecord = bankingService.internalTransfer(request.getFromAccountId(), request.getToAccountId(), request.getAmount());
        return ResponseEntity.ok(transactionRecord);
    }
}
