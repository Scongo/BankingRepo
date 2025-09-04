package za.co.bank.bankx.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import za.co.bank.bankx.constant.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserAccount {
    @Id
    private String id = UUID.randomUUID().toString();


    @Enumerated(EnumType.STRING)
    private AccountType accountType;


    @ManyToOne(optional = false)
    private User user;
    private boolean paymentsEnabled = false;

    private BigDecimal balance = BigDecimal.ZERO;
}
