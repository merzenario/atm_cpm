package com.banco_cpm.atmCpm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banco_cpm.atmCpm.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByIdentification(String identification);

}
