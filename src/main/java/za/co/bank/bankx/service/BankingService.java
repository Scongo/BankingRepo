package za.co.bank.bankx.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.bank.bankx.constant.AccountCategory;
import za.co.bank.bankx.domain.entities.TransactionRecord;
import za.co.bank.bankx.domain.entities.User;
import za.co.bank.bankx.domain.entities.UserAccount;
import za.co.bank.bankx.domain.repository.AccountRepository;
import za.co.bank.bankx.domain.repository.CustomerRepository;
import za.co.bank.bankx.domain.repository.TransactionRepository;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


@Service
public class BankingService {
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;


    public BankingService(CustomerRepository customerRepository,
                          AccountRepository accountRepository,
                          TransactionRepository transactionRepository,
                          NotificationService notificationService) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.notificationService = notificationService;
    }


    @Transactional
    public User onboardCustomer(String firstName, String lastName, String email) {
        User user = new User();
        user.setName(firstName);
        user.setLastName(lastName);
        user.setEmailAddress(email);
        customerRepository.save(user);


// Create Current account (payments enabled)
        UserAccount current = new UserAccount();
        current.setAccountCategory(AccountCategory.CURRENT);
        current.setUser(user);
        current.setBalance(BigDecimal.ZERO);
        current.setPaymentsEnabled(true);
        accountRepository.save(current);


// Create Savings account with joining bonus R500
        UserAccount savings = new UserAccount();
        savings.setAccountCategory(AccountCategory.SAVINGS);
        savings.setUser(user);
        savings.setBalance(new BigDecimal("500.00"));
        savings.setPaymentsEnabled(false);
        accountRepository.save(savings);


// Record joining bonus transaction
        TransactionRecord transactionRecord = new TransactionRecord();
        transactionRecord.setFromAccountId(null);
        transactionRecord.setToAccountId(savings.getId());
        transactionRecord.setAmount(new BigDecimal("500.00"));
        transactionRecord.setFee(BigDecimal.ZERO);
        transactionRecord.setNarration("Joining bonus");
        transactionRecord.setInitiatedBy("SYSTEM_ONBOARD");
        transactionRepository.save(transactionRecord);


        notificationService.notifyCustomer(user.getId(), "Welcome " + user.getName() + ", you have been onboarded. Joining bonus credited to savings.");


        return user;
    }
    public List<UserAccount> getAccountsForCustomer(String customerId) {
        return accountRepository.findByCustomerId(customerId);
    }
    public List<TransactionRecord> getTransactionsForAccount(String accountId) {
        return transactionRepository.findByFromAccountIdOrToAccountIdOrderByTimestampDesc(accountId, accountId);
    }

    @Transactional
    public TransactionRecord internalTransfer(String fromAccId, String toAccId, BigDecimal amount) {
        UserAccount from = accountRepository.findById(fromAccId).orElseThrow(() -> new IllegalArgumentException("From account not found"));
        UserAccount to = accountRepository.findById(toAccId).orElseThrow(() -> new IllegalArgumentException("To account not found"));


        if (!from.isPaymentsEnabled()) throw new IllegalArgumentException("From account not enabled for payments");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be > 0");


// Fee charged on any payment from customer's account: 0.05% of transaction amount
        BigDecimal fee = amount.multiply(new BigDecimal("0.0005")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalDebit = amount.add(fee);


        if (from.getBalance().compareTo(totalDebit) < 0) throw new IllegalArgumentException("Insufficient funds");


        from.setBalance(from.getBalance().subtract(totalDebit));
        to.setBalance(to.getBalance().add(amount));


        accountRepository.save(from);
        accountRepository.save(to);


        TransactionRecord transactionRecord = new TransactionRecord();
        transactionRecord.setFromAccountId(from.getId());
        transactionRecord.setToAccountId(to.getId());
        transactionRecord.setAmount(amount);
        transactionRecord.setFee(fee);
        transactionRecord.setNarration("Internal transfer");
        transactionRecord.setInitiatedBy("INTERNAL");
        transactionRepository.save(transactionRecord);


// If payment into savings, credit 0.5% interest on current balance
        if (to.getAccountCategory() == AccountCategory.SAVINGS) {
            BigDecimal interest = to.getBalance().multiply(new BigDecimal("0.005")).setScale(2, RoundingMode.HALF_UP);
            to.setBalance(to.getBalance().add(interest));
            accountRepository.save(to);
            TransactionRecord interestTr = new TransactionRecord();
            interestTr.setFromAccountId(null);
            interestTr.setToAccountId(to.getId());
            interestTr.setAmount(interest);
            interestTr.setFee(BigDecimal.ZERO);
            interestTr.setNarration("Interest credit for payment received");
            interestTr.setInitiatedBy("SYSTEM_INTEREST");
            transactionRepository.save(interestTr);
            notificationService.notifyCustomer(to.getUser().getId(), "Interest credited: " + interest);
        }


        notificationService.notifyCustomer(from.getUser().getId(), "Debit: " + totalDebit + " (incl fee " + fee + ")");
        notificationService.notifyCustomer(to.getUser().getId(), "Credit: " + amount);


        return transactionRecord;
    }

    @Transactional
    public TransactionRecord processBankZTransaction(String fromAccId, String toAccId, BigDecimal amount, String initiatedBy) {
// Bank Z can debit or credit customer accounts (trusted partner). No payment-enabled restriction.
        UserAccount from = null;
        UserAccount to = null;
        if (fromAccId != null) from = accountRepository.findById(fromAccId).orElseThrow(() -> new IllegalArgumentException("From account not found"));
        if (toAccId != null) to = accountRepository.findById(toAccId).orElseThrow(() -> new IllegalArgumentException("To account not found"));


        BigDecimal fee = BigDecimal.ZERO; // external partner transactions not charged extra by Bank X


        if (from != null) {
            if (from.getBalance().compareTo(amount) < 0) throw new IllegalArgumentException("Insufficient funds for debit");
            from.setBalance(from.getBalance().subtract(amount));
            accountRepository.save(from);
        }
        if (to != null) {
            to.setBalance(to.getBalance().add(amount));
            accountRepository.save(to);


// if credited into savings, apply 0.5% interest
            if (to.getAccountCategory() == AccountCategory.SAVINGS) {
                BigDecimal interest = to.getBalance().multiply(new BigDecimal("0.005")).setScale(2, RoundingMode.HALF_UP);
                to.setBalance(to.getBalance().add(interest));
                accountRepository.save(to);


                TransactionRecord interestTr = new TransactionRecord();
                interestTr.setFromAccountId(null);
                interestTr.setToAccountId(to.getId());
                interestTr.setAmount(interest);
                interestTr.setFee(BigDecimal.ZERO);
                interestTr.setNarration("Interest credit after Bank Z credit");
                interestTr.setInitiatedBy("SYSTEM_INTEREST");
                transactionRepository.save(interestTr);
                notificationService.notifyCustomer(to.getUser().getId(), "Interest credited: " + interest);
            }
        }


        TransactionRecord transactionRecord = new TransactionRecord();
        transactionRecord.setFromAccountId(fromAccId);
        transactionRecord.setToAccountId(toAccId);
        transactionRecord.setAmount(amount);
        transactionRecord.setFee(fee);
        transactionRecord.setNarration("Bank Z processed transaction");
        transactionRecord.setInitiatedBy(initiatedBy == null ? "BANK_Z" : initiatedBy);
        transactionRepository.save(transactionRecord);


        if (from != null) notificationService.notifyCustomer(from.getUser().getId(), "Debit by Bank Z: " + amount);
        if (to != null) notificationService.notifyCustomer(to.getUser().getId(), "Credit by Bank Z: " + amount);


        return transactionRecord;
    }
}
