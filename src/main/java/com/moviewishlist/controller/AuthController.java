package com.moviewishlist.controller;

import com.moviewishlist.model.User;
import com.moviewishlist.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String register(@ModelAttribute User user, Model model) {
        String message = userService.register(user);
        model.addAttribute("message", message);
        return "login"; // redirect to login page after signup
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) {
        if (userService.login(username, password)) {
            return "redirect:/"; // go to home page
        } else {
            model.addAttribute("error", "Invalid username or password!");
            return "login";
        }
    }
}
