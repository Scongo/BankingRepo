package za.co.bank.bankx.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.bank.bankx.domain.entities.TransactionRecord;
import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionRecord, String> {
    List<TransactionRecord> findByFromAccountIdOrToAccountIdOrderByTimestampDesc(String from, String to);
}
