package za.co.bank.bankx.domain.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;


@Entity
@Getter
@Setter
@NoArgsConstructor
public class TransactionRecord {
    @Id
    private String id = UUID.randomUUID().toString();


    private String fromAccountId;
    private String toAccountId;
    private BigDecimal amount;
    private BigDecimal fee;
    private OffsetDateTime timestamp = OffsetDateTime.now();
    private String narration;
    private String initiatedBy; // e.g., INTERNAL, BANK_Z
}
