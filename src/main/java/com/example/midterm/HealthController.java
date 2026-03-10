package com.example.midterm;

import java.util.Map;
import java.util.HashMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
  @GetMapping("/")
  public Map<String, String> home() {
    Map<String, String> response = new HashMap<>();
    response.put("status", "ok");
    response.put("app", "java-app");
    return response;
  }
}
