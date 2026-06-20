# example-hello

Minimal Spring Boot (Java 21) hello-world used to test the homelab HA deploy path
end-to-end. Serves:

- `GET /` → `Hello World from <hostname>` (hostname reveals which node served you)
- `GET /healthcheck` → `OK from <hostname>` (used by ActionsHA monitoring)
- `GET /actuator/health` → Spring Boot actuator health

## Pipeline

GitHub → Jenkins (multibranch, agent label `docker`) → multi-arch image
(`linux/amd64,linux/arm64`) → Gitea registry `gitea.albireo.me/crux/example-hello`
→ deployed on all 3 nodes → Nginx Proxy Manager per node → Cloudflare proxied
multi-A records for `example.albireo.me` → ActionsHA-test health/failover.
