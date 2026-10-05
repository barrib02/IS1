package com.example.demo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class HolaController {
@GetMapping("/")
public String inicio() {
return "Bienvenido a Goodreads Plus";
}
}