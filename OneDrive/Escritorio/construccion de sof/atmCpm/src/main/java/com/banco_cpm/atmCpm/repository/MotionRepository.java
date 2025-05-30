package com.banco_cpm.atmCpm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banco_cpm.atmCpm.entity.Account;
import com.banco_cpm.atmCpm.entity.Motion;

public interface MotionRepository extends JpaRepository<Motion, Long> {
    List<Motion> findByAccount(Account account);

    List<Motion> findByAccountOrderByDateDesc(Account account);
}
