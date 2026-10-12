# Sakai Docker / Compose

Compose-first local development for Sakai on **Java 21** and **Tomcat 10.1**.

The Compose file lives at the **repository root** ([`docker-compose.yml`](../docker-compose.yml)) so the build context is `.` (this checkout), not `docker/..`.

## Requirements

- [Docker](https://docs.docker.com/get-docker/) or [Podman](https://podman.io/) with Compose v2 (`docker compose` / `podman compose`)
- For the **dev** profile: **JDK 21** and Maven 3.9+ on the host (or `./mvnw`)

## Quick start (full image)

From the repository root:

```bash
podman compose up --build
# or: docker compose up --build
```

This starts MariaDB and builds/runs the Sakai image defined by [`Dockerfile`](Dockerfile). Open http://localhost:8080/portal.

## Dev profile (fast iteration)

No image rebuild when you change code. Host Maven deploys exploded artifacts into `.docker-deploy/`, which Tomcat mounts.

```bash
./docker/bin/tomcat.sh up
./docker/bin/deploy.sh                 # first time: full reactor
./docker/bin/tomcat.sh restart
./docker/bin/tomcat.sh logs
```

Day-to-day:

```bash
./docker/bin/deploy.sh -pl assignment -am
./docker/bin/tomcat.sh restart
```

This is **not** Spring Boot live reload — restart Tomcat after Java/component deploys. JPDA is on port `8000`.

Do not run `compose up --build` (default `sakai` service) and `--profile dev` at the same time; both bind `8080`.

## Database only (host Tomcat)

```bash
podman compose up db
```

MariaDB listens on `127.0.0.1:53306`. Point host Tomcat at it using [`tomcat/sakai/sakai.properties.host-example`](tomcat/sakai/sakai.properties.host-example).

| Role | User | Password |
|------|------|----------|
| Root | `root` | `sakairoot` |
| App | `sakai` | `ironchef` |

Reset DB (destructive): `podman compose down -v && podman compose up db`.

## Services

| Service | Profile | Image / build | Ports | Notes |
|---------|---------|---------------|-------|--------|
| `db` | (always) | `mariadb:10` | `127.0.0.1:53306` → `3306` | Init scripts in `mysql/scripts/` |
| `sakai` | (default) | [`Dockerfile`](Dockerfile), context `.` | `8080` | Built from this checkout |
| `tomcat` | `dev` | `tomcat:10.1-jdk21-temurin` | `8080`, `8000` | Mounts `.docker-deploy/{webapps,components,lib}` |

Helpers: [`bin/deploy.sh`](bin/deploy.sh), [`bin/tomcat.sh`](bin/tomcat.sh).

## JDBC hosts

| Where Tomcat runs | JDBC host |
|-------------------|-----------|
| Compose `sakai` / `tomcat` | `db` ([`tomcat/sakai/sakai.properties`](tomcat/sakai/sakai.properties)) |
| Host process + Compose `db` | `127.0.0.1:53306` ([host-example](tomcat/sakai/sakai.properties.host-example)) |

## Dockerfile

Multi-stage build used by the default `sakai` service:

1. **Build** — `maven:3.9-eclipse-temurin-21`, `sakai:deploy-exploded` into `/deploy`
2. **Runtime** — `tomcat:10.1-jdk21-temurin` with conf from `docker/tomcat/`

`COPY docker/...` paths are relative to the **repo root** build context (not because Compose is nested under `docker/`).

## Dev Containers

[`.devcontainer/`](../.devcontainer/) reuses the root Compose file and adds a lightweight `dev` workspace service.

## Troubleshooting

- **`up --build` does nothing for Sakai** — run from the **repo root** (`docker-compose.yml` is there), not only inside `docker/`.
- **Port in use** — stop whatever is on `8080`, `8000`, or `53306`.
- **Empty portal on dev profile** — run `./docker/bin/deploy.sh` then `./docker/bin/tomcat.sh restart`.
- **DB init only on empty volume** — `podman compose down -v` to recreate.
