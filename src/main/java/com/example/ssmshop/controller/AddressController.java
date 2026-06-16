package com.example.ssmshop.controller;

import com.example.ssmshop.domain.Address;
import com.example.ssmshop.form.AddressForm;
import com.example.ssmshop.service.AddressService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AddressController extends BaseController {
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/addresses")
    public String addresses(HttpSession session, Model model) {
        model.addAttribute("addresses", addressService.list(currentUserId(session)));
        return "user/addresses";
    }

    @GetMapping("/addresses/new")
    public String createPage(Model model) {
        model.addAttribute("addressForm", new AddressForm());
        model.addAttribute("action", "/addresses");
        return "user/address-form";
    }

    @PostMapping("/addresses")
    public String create(@Valid AddressForm addressForm, BindingResult bindingResult, HttpSession session, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("action", "/addresses");
            return "user/address-form";
        }
        addressService.save(currentUserId(session), null, addressForm);
        return "redirect:/addresses";
    }

    @GetMapping("/addresses/{id}/edit")
    public String editPage(@PathVariable Long id, HttpSession session, Model model) {
        Address address = addressService.find(id, currentUserId(session));
        model.addAttribute("addressForm", addressService.toForm(address));
        model.addAttribute("action", "/addresses/" + id);
        return "user/address-form";
    }

    @GetMapping("/addresses/{id}")
    public String editFallback(@PathVariable Long id) {
        return "redirect:/addresses/" + id + "/edit";
    }

    @PostMapping("/addresses/{id}")
    public String edit(@PathVariable Long id, @Valid AddressForm addressForm, BindingResult bindingResult,
                       HttpSession session, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("action", "/addresses/" + id);
            return "user/address-form";
        }
        addressService.save(currentUserId(session), id, addressForm);
        return "redirect:/addresses";
    }

    @GetMapping("/addresses/{id}/delete")
    public String deleteFallback() {
        return "redirect:/addresses";
    }

    @PostMapping("/addresses/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        addressService.delete(id, currentUserId(session));
        return "redirect:/addresses";
    }
}
