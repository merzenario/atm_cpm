package com.banco_cpm.atmCpm.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.banco_cpm.atmCpm.entity.Account;
import com.banco_cpm.atmCpm.entity.AccountType;
import com.banco_cpm.atmCpm.entity.Customer;
import com.banco_cpm.atmCpm.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    // Método para crear un cliente
    public Account createAccount(Customer customer, String number, AccountType type, double openingBalance) {
        Account account = Account.builder()
                .customer(customer)
                .number(number)
                .type(type)
                .balance(openingBalance)
                .build();

        return accountRepository.save(account);
    }

    // buscar cuenta por número
    public Optional<Account> searchbyNumber(String number) {
        return accountRepository.findByNumber(number);
    }

    // consultar saldo de una cuenta
    public double ConsultBalance(Account account) {
        return account.getBalance();
    }

    // obtener cuentas de un cliente
    public List<Account> obtainAccountsCustomer(Customer customer) {
        return customer.getAccounts();
    }

    // Actualizar saldo de una cuenta
    public void updateBalance(Account account, double newBalance) {
        account.setBalance(newBalance);
        accountRepository.save(account);
    }

    // Buscar cuenta por cliente
    public List<Account> searchByCustomer(Customer customer) {
        return accountRepository.findByCustomer(customer);
    }

    // Obtener cuenta por cliente actual
    public Account obtainAccountByCustomerCurrent(String username) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

}
