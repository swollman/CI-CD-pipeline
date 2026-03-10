package com.example.midterm;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
  @GetMapping("/")
  public Map<String, String> home() {
    return Map.of("status", "ok", "app", "java-app");
  }
}
