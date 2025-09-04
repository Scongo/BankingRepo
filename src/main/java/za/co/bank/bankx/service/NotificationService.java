package za.co.bank.bankx.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    //TODO push notifications to Kafka or send email.
    public void notifyCustomer(String customerId, String message) {
        System.out.println("[NOTIFICATION] customer=" + customerId + " msg=" + message);
    }
}
