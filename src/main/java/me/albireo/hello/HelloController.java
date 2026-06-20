package me.albireo.hello;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
public class HelloController {

    @GetMapping("/")
    public String hello() {
        return "Hello World from " + hostname() + "\n";
    }

    // House convention health endpoint (matches conch). Actuator also serves /actuator/health.
    @GetMapping("/healthcheck")
    public String healthcheck() {
        return "OK from " + hostname() + "\n";
    }

    private String hostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}
