package za.co.bank.bankx.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.bank.bankx.constant.AccountType;
import za.co.bank.bankx.domain.entities.UserAccount;
import java.util.List;

public interface AccountRepository extends JpaRepository<UserAccount, String> {
    List<UserAccount> findByUserId(String userId);
    List<UserAccount> findByAccountTypeAndUserId(AccountType type, String userId);
}
