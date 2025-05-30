package com.banco_cpm.atmCpm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banco_cpm.atmCpm.entity.Account;
import com.banco_cpm.atmCpm.entity.Customer;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByNumber(String number);

    List<Account> findByCustomer(Customer customer);
}
