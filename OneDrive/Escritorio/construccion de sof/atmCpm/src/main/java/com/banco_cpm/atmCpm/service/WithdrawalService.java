package com.banco_cpm.atmCpm.service;

import org.springframework.stereotype.Service;

import com.banco_cpm.atmCpm.entity.Account;
import com.banco_cpm.atmCpm.entity.Customer;
import com.banco_cpm.atmCpm.repository.AccountRepository;
import com.banco_cpm.atmCpm.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WithdrawalService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final MotionService motionService;

    public String makeWithdrawal(String identification, String accountNumber, double amount) {
        Customer customer = customerRepository.findByIdentification(identification)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Account account = accountRepository.findByNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        if (!account.getCustomer().equals(customer)) {
            throw new RuntimeException("La cuenta no es del cliente");
        }

        if (customer.isBlock()) {
            throw new RuntimeException("Su cuenta fue bloqueada");
        }

        boolean exito = motionService.makeWithdrawal(account, amount);

        if (!exito) {
            throw new RuntimeException("No se pudo realizar el retiro, sin saldo suficiente");
        }

        return "redirect:account/menu?message=Retiro realizado con exito";

    }
}
