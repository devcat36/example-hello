package me.albireo.hello;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

@RestController
public class HelloController {

    private final JdbcTemplate jdbc;

    public HelloController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private String hostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

    // DB-independent health endpoint (used by ActionsHA monitoring).
    @GetMapping("/healthcheck")
    public String healthcheck() {
        return "OK from " + hostname() + "\n";
    }

    // Read the single global counter.
    @GetMapping(value = "/api/counter", produces = "application/json")
    public Map<String, Object> getCounter() {
        Integer v = jdbc.queryForObject("SELECT value FROM counter WHERE id = 1", Integer.class);
        return Map.of("value", v, "node", hostname());
    }

    // Atomically increment the single global counter and return the new value.
    @PostMapping(value = "/api/counter/increment", produces = "application/json")
    public Map<String, Object> increment() {
        Integer v = jdbc.queryForObject(
                "UPDATE counter SET value = value + 1 WHERE id = 1 RETURNING value", Integer.class);
        return Map.of("value", v, "node", hostname());
    }

    // Page with the number + Increment button.
    @GetMapping(value = "/", produces = "text/html")
    public String index() {
        String host = hostname();
        return """
                <!doctype html>
                <html><head><meta charset="utf-8"><title>example.albireo.me</title>
                <style>
                  body{font-family:system-ui,-apple-system,sans-serif;text-align:center;margin-top:11vh;color:#1c1c1c}
                  h1{font-weight:600;color:#444}
                  #n{font-size:6rem;font-weight:800;margin:1rem;color:#2d6cdf}
                  button{font-size:1.4rem;padding:.6rem 1.6rem;border:0;border-radius:12px;background:#2d6cdf;color:#fff;cursor:pointer}
                  button:active{transform:translateY(1px)}
                  .m{color:#999;margin-top:2.5rem;font-size:.85rem}
                </style></head>
                <body>
                  <h1>Hello from %s</h1>
                  <div id="n">…</div>
                  <button onclick="inc()">Increment</button>
                  <p class="m">one global counter in CockroachDB · this response served by <b>%s</b></p>
                  <script>
                    async function load(){const r=await fetch('/api/counter');document.getElementById('n').textContent=(await r.json()).value;}
                    async function inc(){const r=await fetch('/api/counter/increment',{method:'POST'});document.getElementById('n').textContent=(await r.json()).value;}
                    load();
                  </script>
                </body></html>
                """.formatted(host, host);
    }
}
