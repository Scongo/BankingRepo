package za.co.bank.bankx.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import za.co.bank.bankx.domain.entities.TransactionRecord;
import za.co.bank.bankx.domain.entities.User;
import za.co.bank.bankx.domain.entities.UserAccount;
import za.co.bank.bankx.dto.OnboardRequest;
import za.co.bank.bankx.service.BankingService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    private final BankingService bankingService;

    @Autowired
    public UserController(BankingService bankingService) {
        this.bankingService = bankingService;
    }


    @PostMapping("/onboard")
    public ResponseEntity<User> onboard(@Valid @RequestBody OnboardRequest request) {
        User user = bankingService.onboardCustomer(request.getFirstName(), request.getLastName(), request.getEmail());
        return ResponseEntity.ok(user);
    }


    @GetMapping("/{userId}/accounts")
    public ResponseEntity<List<UserAccount>> getAccounts(@PathVariable String userId) {
        return ResponseEntity.ok(bankingService.getAccountsForCustomer(userId));
    }


    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<List<TransactionRecord>> getTransactions(@PathVariable String accountId) {
        return ResponseEntity.ok(bankingService.getTransactionsForAccount(accountId));
    }
}