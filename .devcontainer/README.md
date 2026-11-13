# Sakai Dev Container

Thin VS Code / Codespaces / Dev Containers setup that reuses the root [`docker-compose.yml`](../docker-compose.yml).

- Starts the Compose **`db`** (MariaDB) service
- Opens a **`dev`** workspace with JDK 21 and Maven
- Downloads Tomcat 10.1 into `~/sakai-tomcat` and points JDBC at hostname `db`
- Does **not** start the baked `sakai` image by default (from the repo root: `podman compose build sakai && podman compose --profile full up -d`)

## Usage

1. Open this repository in VS Code or Codespaces.
2. **Dev Containers: Reopen in Container**.
3. Wait for `setup.sh` to finish.
4. In a terminal:

```bash
mcid      # build + deploy-exploded to ~/sakai-tomcat
tstart    # start Tomcat
tlog      # follow catalina.out
```

5. Open http://localhost:8080/portal

For Compose-only workflows (no IDE container), see [`docker/README.md`](../docker/README.md).
