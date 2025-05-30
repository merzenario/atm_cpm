package com.banco_cpm.atmCpm.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.banco_cpm.atmCpm.entity.Customer;
import com.banco_cpm.atmCpm.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Customer createCustomer(Customer customer) {
        customer.setBlock(false);
        customer.setFailedAttempts(0);
        return customerRepository.save(customer);
    }

    public Optional<Customer> searchByIdentification(String identification) {
        return customerRepository.findByIdentification(identification);
    }

    public boolean validatePin(Customer customer, String pin) {
        if (customer.isBlock())
            return false;

        if (customer.getPin().equals(pin)) {
            customer.setFailedAttempts(0);
            customerRepository.save(customer);
            return true;
        } else {
            int attempts = customer.getFailedAttempts() + 1;
            customer.setFailedAttempts(attempts);
            if (attempts >= 3) {
                customer.setBlock(true);
            }
            customerRepository.save(customer);
            return false;
        }
    }

    public void unlockCustomer(String identification, String newPin) {
        Optional<Customer> optionalCustomer = customerRepository.findByIdentification(identification);
        if (optionalCustomer.isPresent()) {
            Customer customer = optionalCustomer.get();
            customer.setBlock(false);
            customer.setFailedAttempts(0);
            customer.setPin(newPin);
            customerRepository.save(customer);
        }
    }

    public void changePin(Customer customer, String newPin) {
        customer.setPin(newPin);
        customerRepository.save(customer);
    }

    public void increaseAttempts(Customer customer) {
        customer.setAttempts(customer.getAttempts() + 1);
        customerRepository.save(customer);
    }

    public void resetAttempts(Customer customer) {
        customer.setAttempts(0);
        customerRepository.save(customer);
    }

    public void blockCustomer(Customer customer) {
        customer.setBlock(true);
        customerRepository.save(customer);
    }

}
