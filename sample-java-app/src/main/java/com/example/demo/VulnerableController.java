package com.example.demo;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This controller intentionally contains insecure patterns to demonstrate what scanners should find.
 * Comments above each endpoint describe the vulnerability and why it should be fixed.
 */
@RestController
public class VulnerableController {

    // XSS: echoes back user input inside HTML without encoding
    @GetMapping(value = "/xss", produces = MediaType.TEXT_HTML_VALUE)
    public String xss(@RequestParam(defaultValue = "guest") String name) {
        // vulnerable: unescaped user input included in HTML
        return "<html><body>Hello " + name + "</body></html>";
    }

    // SQL-like vulnerability: builds query using string concatenation
    @GetMapping("/sql")
    public String sql(@RequestParam(defaultValue = "guest") String name) {
        // imitation of SQL injection vulnerability (no actual DB used here)
        String query = "SELECT * FROM users WHERE name = '" + name + "'";
        return "Built query (vulnerable): " + query;
    }

    // Hard-coded credential to demonstrate detection by static scanners
    @GetMapping("/auth")
    public String hardcodedSecret() {
        String pwd = "Password123!"; // intentionally hard-coded secret
        return "Sample hardcoded password: " + pwd;
    }

    // Simple session login that stores a session id in a non-secure cookie.
    // Vulnerability: cookie set without HttpOnly or Secure flags and session store is in-memory map
    // Why fix: session hijacking is easier; use secure, HttpOnly cookies and a proper server-side session store.
    @GetMapping("/session-login")
    public String sessionLogin(HttpServletResponse resp) {
        String sessionId = SessionManager.createSession("user1");
        Cookie c = new Cookie("SESSIONID", sessionId);
        // Intentionally not setting HttpOnly/Secure to demonstrate a vulnerability
        resp.addCookie(c);
        return "Logged in, session set (insecure cookie).";
    }

    // Endpoint that executes a system command built from user input.
    // Vulnerability: Runtime.exec with user input allows command injection.
    // Why fix: always validate/whitelist input and avoid executing shell commands; use safe libraries.
    @GetMapping("/exec")
    public String exec(@RequestParam(defaultValue = "date") String cmd) throws IOException {
        // Dangerous: directly passing user input to shell
        Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
        try (var is = p.getInputStream()) {
            return new String(is.readAllBytes());
        }
    }

    // File write that demonstrates directory traversal vulnerability when filename is not sanitized.
    // Vulnerability: attacker can write to arbitrary path if they control filename
    // Why fix: sanitize filenames and restrict write directory.
    @PostMapping(value = "/upload", consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String upload(@RequestParam String filename, @RequestBody String body) throws IOException {
        // Intentionally allowing arbitrary paths — vulnerable to path traversal
        Path base = Paths.get("/tmp/uploads");
        Files.createDirectories(base);
        Path dest = base.resolve(filename);
        try (FileOutputStream fos = new FileOutputStream(dest.toFile())) {
            fos.write(body.getBytes());
        }
        return "Wrote file: " + dest.toString();
    }

    // Open redirect demonstration. Redirects to user controlled URL.
    // Vulnerability: open redirect can aid phishing and bypasses filters.
    // Why fix: validate allowed redirect targets or use relative paths only.
    @GetMapping("/redirect")
    public String redirect(@RequestParam String to) {
        return "Redirecting to: " + to; // for demo we just echo; a real redirect would use ResponseEntity
    }

}
