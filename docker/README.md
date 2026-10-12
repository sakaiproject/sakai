# Sakai Docker / Compose

Compose-first local development for Sakai on **Java 21** and **Tomcat 10.1**.

## Requirements

- [Docker](https://docs.docker.com/get-docker/) with Compose v2 (`docker compose`)
- For the **dev** profile: **JDK 21** and Maven 3.9+ on the host (or use `./mvnw` from the repo root)

## Quick start (recommended: dev profile)

Iterative coding without rebuilding images. From this `docker/` directory:

```bash
# 1. Start MariaDB + Tomcat (empty deploy dir is fine at first)
./bin/tomcat.sh up

# 2. Full build + exploded deploy into ../.docker-deploy (slow once)
./bin/deploy.sh

# 3. Restart Tomcat to pick up the deploy
./bin/tomcat.sh restart
./bin/tomcat.sh logs
```

Open http://localhost:8080/portal.

### Day-to-day change loop

After editing a module (example: Assignments):

```bash
./bin/deploy.sh -pl assignment -am
./bin/tomcat.sh restart
```

This is **not** Spring Boot live reload. Sakai still needs a Tomcat restart after Java/component deploys. The win is: no Docker image rebuild, module-scoped Maven, exploded deploy, JPDA on port `8000` for IDE HotSwap of method bodies.

### Database only (host Tomcat)

```bash
docker compose up db
```

MariaDB listens on `127.0.0.1:53306`. Point host Tomcat at it using [`tomcat/sakai/sakai.properties.host-example`](tomcat/sakai/sakai.properties.host-example):

```properties
url@javax.sql.BaseDataSource=jdbc:mariadb://127.0.0.1:53306/sakai?useUnicode=true&characterEncoding=UTF-8
username@javax.sql.BaseDataSource=sakai
password@javax.sql.BaseDataSource=ironchef
```

| Role | User | Password |
|------|------|----------|
| Root | `root` | `sakairoot` |
| App | `sakai` | `ironchef` |

Reset the database (destructive):

```bash
docker compose down -v
docker compose up db
```

### Full baked image (optional)

Clean / CI-style run that compiles inside Docker (slow; not for daily coding):

```bash
docker compose --profile image up --build
```

## Services

| Service | Profile | Image / build | Ports | Notes |
|---------|---------|---------------|-------|--------|
| `db` | (always) | `mariadb:10` | `127.0.0.1:53306` → `3306` | Init scripts in `mysql/scripts/` |
| `tomcat` | `dev` | `tomcat:10.1-jdk21-temurin` | `8080`, `8000` (JPDA) | Mounts `../.docker-deploy/{webapps,components,lib}` |
| `sakai` | `image` | build via [`Dockerfile`](Dockerfile) | `8080` | All-in-one image; slow rebuild |

Helpers: [`bin/deploy.sh`](bin/deploy.sh), [`bin/tomcat.sh`](bin/tomcat.sh).

## JDBC hosts

| Where Tomcat runs | JDBC host |
|-------------------|-----------|
| Compose `tomcat` / `sakai` service | `db` ([`tomcat/sakai/sakai.properties`](tomcat/sakai/sakai.properties)) |
| Host process + Compose `db` only | `127.0.0.1:53306` ([host-example](tomcat/sakai/sakai.properties.host-example)) |

## Dockerfile

[`Dockerfile`](Dockerfile) is used only by the `image` profile:

1. **Build** — `maven:3.9-eclipse-temurin-21`, `sakai:deploy-exploded` into `/deploy`
2. **Runtime** — `tomcat:10.1-jdk21-temurin` with conf from `tomcat/`

## Dev Containers

[`.devcontainer/`](../.devcontainer/) is a thin VS Code / Codespaces wrapper around this Compose file. Prefer the **dev** profile above if you already have JDK/Maven on the host.

## Troubleshooting

- **Port in use** — stop whatever is bound to `8080`, `8000`, or `53306`.
- **Empty portal / missing apps** — run `./bin/deploy.sh` then `./bin/tomcat.sh restart`.
- **DB user missing after SQL changes** — init scripts run only on an empty volume; `docker compose down -v` then bring services back.
- **Sakai cannot reach DB in Compose** — hostname must be `db`, not `localhost`.
- **Host Tomcat cannot reach DB** — use `127.0.0.1:53306` with `docker compose up db`.
- **Do not use `--profile dev` and `--profile image` together** — both bind host port `8080`.
