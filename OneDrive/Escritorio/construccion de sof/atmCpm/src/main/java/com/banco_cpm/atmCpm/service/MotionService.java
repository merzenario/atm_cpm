package com.banco_cpm.atmCpm.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.banco_cpm.atmCpm.entity.Account;
import com.banco_cpm.atmCpm.entity.Motion;
import com.banco_cpm.atmCpm.entity.MotionType;
import com.banco_cpm.atmCpm.repository.AccountRepository;
import com.banco_cpm.atmCpm.repository.MotionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MotionService {

    private final MotionRepository motionRepository;
    private final AccountRepository accountRepository;

    // registrar movimiento
    public Motion registerMotion(Account account, MotionType type, double amount) {
        Motion motion = Motion.builder()
                .account(account)
                .type(type)
                .amount(amount)
                .date(LocalDateTime.now())
                .build();
        return motionRepository.save(motion);
    }

    // obtener movimientos por cuenta
    public List<Motion> obtainMotionsByAccount(Account account, double amount) {
        return motionRepository.findByAccount(account);
    }

    // realizar deposito en cuenta
    public boolean makeWithdrawal(Account account, double amount) {
        if (account.getBalance() >= amount) {
            account.setBalance(account.getBalance() - amount);
            accountRepository.save(account);
            registerMotion(account, MotionType.WITHDRAWAL, amount);
            return true;
        }
        return false;
    }

    // realizar transferencia entre cuentas
    public boolean makeTransfer(Account origen, Account destination, double amount) {
        if (origen.getBalance() >= amount) {
            origen.setBalance(destination.getBalance() - amount);
            destination.setBalance(destination.getBalance() + amount);
            accountRepository.save(origen);
            accountRepository.save(destination);

            registerMotion(origen, MotionType.TRANSFER, amount);
            registerMotion(destination, MotionType.TRANSFER, amount);

            return true;
        }
        return false;
    }

    // busar cuenta por numero
    public List<Motion> searchByAccount(String accountNumber) {
        Account account = accountRepository.findByNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        return motionRepository.findByAccountOrderByDateDesc(account);
    }

    // realizar deposito en cuenta
    public boolean makeDeposit(Account account, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }
        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);
        registerMotion(account, MotionType.DEPOSIT, amount);
        return true;
    }

}