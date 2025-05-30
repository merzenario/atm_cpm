package com.banco_cpm.atmCpm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.banco_cpm.atmCpm.entity.Account;
import com.banco_cpm.atmCpm.entity.AccountType;
import com.banco_cpm.atmCpm.entity.Customer;
import com.banco_cpm.atmCpm.service.AccountService;
import com.banco_cpm.atmCpm.service.CustomerService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final CustomerService customerService;
    private final AccountService accountService;

    // Mostrar página de inicio del admin
    @GetMapping
    public String adminHome() {
        return "admin/index";
    }

    // Mostrar formulario para crear un cliente
    @GetMapping("/create-customer")
    public String showCreateCustomerFom(Model model) {
        model.addAttribute("customer", new Customer());
        return "admin/create-customer";
    }

    // procesa el formulario para crear un cliente
    @PostMapping("/create-customer")
    public String createCustomer(@ModelAttribute Customer customer) {
        customerService.createCustomer(customer);
        return "redirect:/admin";
    }

    // Mostrar formulario para crear una cuenta
    @GetMapping("/create-account")
    public String showCreateAccountForm(Model model) {
        model.addAttribute("account", new Account());
        return "admin/create-account";
    }

    // Procesa el formulario para crear una cuenta
    @PostMapping("/create-account")
    public String createAccount(@RequestParam String identification, @RequestParam String number,
            @RequestParam AccountType type, @RequestParam double balance) {
        Customer customer = customerService.searchByIdentification(identification).orElseThrow();
        accountService.createAccount(customer, number, type, balance);

        return "redirect:/admin";
    }

    // Desbloquear una cuenta
    @GetMapping("/unlock")
    public String showUnlock() {
        return "admin/unlock";
    }

    // Procesa para desbloquear una cuenta
    @PostMapping("/unlock")
    public String unlockAccount(@RequestParam String identification, @RequestParam String newPin) {
        customerService.unlockCustomer(identification, newPin);
        return "redirect:/admin";

    }

}
