package com.banco_cpm.atmCpm.dto;

import lombok.Data;

@Data
public class TransferForm {
    private String sourceAccount;
    private String destinationAccount;
    private double amount;

}
