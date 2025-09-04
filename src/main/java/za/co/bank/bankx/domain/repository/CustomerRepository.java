package za.co.bank.bankx.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.bank.bankx.domain.entities.User;

public interface CustomerRepository extends JpaRepository<User, String> {
}
