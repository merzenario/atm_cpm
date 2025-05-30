package com.banco_cpm.atmCpm.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true)
    private String identification;
    private String pin;
    private boolean block;
    private int failedAttempts;
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Account> accounts;

    public int getAttempts() {
        return this.failedAttempts;
    }

    public void setAttempts(int attempts) {
        this.failedAttempts = attempts;
    }

    public void increaseAttempts() {
        this.failedAttempts++;
    }

    public void resetAttempts() {
        this.failedAttempts = 0;
    }

    public String getFullName() {
        return this.name;
    }
}
