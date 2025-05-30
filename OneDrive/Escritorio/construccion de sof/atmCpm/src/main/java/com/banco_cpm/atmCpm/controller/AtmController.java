package com.banco_cpm.atmCpm.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.banco_cpm.atmCpm.dto.TransferForm;
import com.banco_cpm.atmCpm.entity.Account;
import com.banco_cpm.atmCpm.entity.Customer;
import com.banco_cpm.atmCpm.repository.AccountRepository;
import com.banco_cpm.atmCpm.service.AccountService;
import com.banco_cpm.atmCpm.service.CustomerService;
import com.banco_cpm.atmCpm.service.MotionService;
import com.banco_cpm.atmCpm.service.WithdrawalService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/atm")
public class AtmController {
    private final CustomerService customerService;
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final MotionService motionService;
    private final WithdrawalService withdrawalService;

    // Mostrar formulario de inicio de sesión
    @GetMapping
    public String loginForm() {
        return "atm/index";
    }

    // Procesar el inicio de sesión
    @PostMapping("/login")
    public String login(@RequestParam String accountNumber, @RequestParam String pin, HttpSession session,
            Model model) {
        var account = accountService.searchbyNumber(accountNumber);
        if (account.isEmpty()) { // Verifica si la cuenta existe
            model.addAttribute("error", "Cuenta Inexistente");
            return "atm/login";
        }

        Customer customer = account.get().getCustomer();

        // Verifica si el cliente está bloqueado
        if (customer.isBlock()) {
            model.addAttribute("error", "Cuenta bloqueada");
            return "atm/login";
        }

        // Verifica si el cliente ha alcanzado el número máximo de intentos
        if (!customer.getPin().equals(pin)) {
            customerService.increaseAttempts(customer);
            if (customer.getAttempts() >= 3) {
                customerService.blockCustomer(customer);
                model.addAttribute("error", "Cuenta bloqueada por demasiados intentos fallidos");

            } else {
                model.addAttribute("error", "PIN incorrecto");
            }
            return "atm/login";
        }

        // Si el PIN es correcto, reinicia los intentos y guarda la sesión
        customerService.resetAttempts(customer);
        session.setAttribute("customer", customer);
        return "redirect:/atm/menu";

    }

    // Mostrar menu principal
    @GetMapping("/menu")
    public String menu(HttpSession session, Model model) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null)
            return "redirect:/atm";

        model.addAttribute("customer", customer);
        model.addAttribute("accounts", accountService.searchByCustomer(customer));
        return "atm/menu";
    }

    // Mostrar formulario de consulta
    @GetMapping("/consult")
    public String consult(Model model, HttpSession session) {
        Customer customer = (Customer) session.getAttribute("customer");
        model.addAttribute("customer", accountService.searchByCustomer(customer));
        return "atm/consult";
    }

    // mostrar movimientos de una cuenta
    @GetMapping("/motion/{number}")
    public String motion(@PathVariable String number, Model model, HttpSession session) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null)
            return "redirect:/atm";

        try {
            var motions = motionService.searchByAccount(number);
            model.addAttribute("motions", motions);
            return "atm/motion";
        } catch (Exception e) {
            model.addAttribute("error", "Error al obtener los movimientos: " + e.getMessage());
            return "atm/consult";
        }
    }

    // mostrar usario invalidado, salida de la sesión
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Invalida la sesión actual
        return "redirect:/atm";
    }

    // Mostrar formulario de retiro
    @GetMapping("/withdrawal")
    public String showWithdrawalForm(Model model, HttpSession session) {
        Customer customer = (Customer) session.getAttribute("customer");
        model.addAttribute("customer", accountService.searchByCustomer(customer));
        return "atm/withdrawal";
    }

    // Procesar el retiro
    @PostMapping("/withdrawal")
    public String makeWithdrawal(@RequestParam String identification, @RequestParam String accountNumber,
            @RequestParam double amount, RedirectAttributes redirectAttributes) {
        try {
            String result = withdrawalService.makeWithdrawal(identification, accountNumber, amount);
            redirectAttributes.addFlashAttribute("success", "Retiro exitoso");
            return result;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/atm/withdrawal";
        }
    }

    // Mostrar formulario de consignación
    @GetMapping("/consign")
    public String showConsignForm(Model model, HttpSession session) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return "redirect:/atm";
        }
        return "atm/consign";
    }

    // Procesa la consignación
    @PostMapping("/deposit")
    public String deposit(@RequestParam String accountNumber, @RequestParam double amount, Model model) {
        // Verificar si la cuenta existe
        try {
            Account account = accountRepository.findByNumber(accountNumber)
                    .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
            // Verificar si el monto es válido
            motionService.makeDeposit(account, amount);
            model.addAttribute("message", "Consignación exitosa");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "atm/deposit";
    }

    // Mostrar formulario de transferencia
    @GetMapping("/transfer")
    public String showTransferForm(Model model) {
        model.addAttribute("transferForm", new TransferForm());
        return "atm/transfer";
    }

    // Procesa la transferencia
    @PostMapping("/transfer")
    public String trsnfer(@RequestParam String accountNumberDestination, @RequestParam double amount,
            HttpSession session, Model model) {
        Customer customer = (Customer) session.getAttribute("customer");
        // Verifica si el cliente sea válido
        if (customer == null)
            return "redirect:/atm";

        Account origin = accountService.searchByCustomer(customer)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No se encontro la cuenta origen"));
        // Verifica si la cuenta destino existe
        try {
            Account destination = accountService.searchbyNumber(accountNumberDestination)
                    .orElseThrow(() -> new RuntimeException("Cuenta destino no encontrada"));
            // verifica si el monto es válido
            if (motionService.makeTransfer(origin, destination, amount)) {
                model.addAttribute("message", "Transferencia realizada con éxito");
            } else {
                model.addAttribute("error", "Saldo insuficiente para realizar la transferencia");
            }
        } catch (Exception e) { // Manejo de excepciones
            model.addAttribute("error", "Error: " + e.getMessage());
        }
        return "atm/transfer";
    }

    // obtener el titular de una cuenta por su número
    @GetMapping("/holder")
    @ResponseBody
    public Map<String, String> obtainHolder(@RequestParam String number) {
        return accountService.searchbyNumber(number)
                .map(account -> Map.of("nombre", account.getCustomer().getFullName()))
                .orElse(Map.of());
    }

    // mostrar formulario de cambio de clave
    @GetMapping("/password-change")
    public String showPasswordChangeForm() {
        return "atm/password-change";
    }

    @PostMapping("/password-change")
    public String passwordChange(@RequestParam String currentPassword, @RequestParam String newPassword,
            @RequestParam String passwordCheck, HttpSession session, Model model) {
        Customer customer = (Customer) session.getAttribute("Cliente");
        // verifica cliente
        if (customer == null)
            return "redirect:/atm";

        // verifica si la contraseña actual es correcta
        if (!customer.getPin().equals(currentPassword)) {
            model.addAttribute("error", "Clave actual incorrecta.");
            return "atm/password-change";
        }

        // verifica el cambio de clave
        if (!newPassword.equals(passwordCheck)) {
            model.addAttribute("error", "Las nuevas claves no coinciden.");
            return "atm/password-change";
        }

        customerService.changePin(customer, newPassword);

        session.setAttribute("cliente", customer);

        model.addAttribute("mensaje", "Clave cambiada exitosamente.");
        return "atm/password-change";
    }

}