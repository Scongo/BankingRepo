package za.co.bank.bankx.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.bank.bankx.constant.AccountCategory;
import za.co.bank.bankx.domain.entities.UserAccount;
import java.util.List;

public interface AccountRepository extends JpaRepository<UserAccount, String> {
    List<UserAccount> findByCustomerId(String customerId);
    List<UserAccount> findByAccountTypeAndCustomerId(AccountCategory type, String customerId);
}
