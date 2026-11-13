# Sakai Docker / Compose

Compose-first local development for Sakai on **Java 21** and **Tomcat 10.1**.

The Compose file is at the repository root ([`docker-compose.yml`](../docker-compose.yml)). The build context is the checkout root.

## Requirements

- [Docker](https://docs.docker.com/get-docker/) or [Podman](https://podman.io/) with Compose v2 (`docker compose` / `podman compose`)
- **VM memory:** at least **8 GB** for the Podman machine or Docker Desktop (10 GB recommended). Runtime Tomcat uses a 4 GB heap (`tomcat/bin/setenv.sh`); MariaDB and the OS share the same VM.
  - Podman: `podman machine stop && podman machine set --memory 10240 && podman machine start`
  - Docker Desktop: Settings → Resources → Memory
- For the **dev** profile: **JDK 21** and Maven 3.9+ on the host; [`bin/deploy.sh`](bin/deploy.sh) uses `mvn` when on `PATH`, otherwise [`mvnw`](../mvnw)

## Quick start (full image)

From the repository root:

```bash
podman compose build sakai
podman compose --profile full up -d
podman compose logs -f sakai
# or the same with docker compose
```

Wait for “Server startup in …” then open http://localhost:8080/portal (Sakai’s main UI — not a separate product; the `/portal` webapp in this checkout).

This starts MariaDB and the Sakai image from [`Dockerfile`](Dockerfile) (tagged `sakai:local`).

## Dev profile (fast iteration)

Host Maven deploys exploded artifacts into `.docker-deploy/`, which Tomcat mounts. No image rebuild when you change code.

```bash
./docker/bin/tomcat.sh up
./docker/bin/deploy.sh
./docker/bin/tomcat.sh restart
./docker/bin/tomcat.sh logs
```

Day-to-day:

```bash
./docker/bin/deploy.sh -pl assignment -am
./docker/bin/tomcat.sh restart
```

Restart Tomcat after Java/component deploys. JPDA is on port `8000`.

Use [`bin/tomcat.sh`](bin/tomcat.sh) for the dev Tomcat (`up` starts `db` + `tomcat` only). Do not run `--profile full` and `--profile dev` together; both bind `127.0.0.1:8080`.

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
| `sakai` | `full` | image `sakai:local` via [`Dockerfile`](Dockerfile) | `127.0.0.1:8080` | Built from this checkout |
| `tomcat` | `dev` | `tomcat:10.1-jdk21-temurin` | `127.0.0.1:8080`, `127.0.0.1:8000` | Mounts `.docker-deploy` webapps/components and lib → `sakai-lib` |

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

`COPY` paths in the Dockerfile are relative to the repo-root build context.

### Build memory

`MAVEN_OPTS` and `MAVEN_THREADS` are set in [`Dockerfile`](Dockerfile). Maven uses ~75% of the build container’s RAM and caps parallel modules at 2.

If you get `Java heap space`:

1. Give the Podman machine / Docker Desktop VM more RAM (8 GB+).
2. Rebuild with a fixed heap, for example:

```bash
podman compose build --build-arg MAVEN_OPTS="-Xms2g -Xmx8g -XX:+TieredCompilation -XX:TieredStopAtLevel=1" sakai
```

## Dev Containers

[`.devcontainer/`](../.devcontainer/) reuses the root Compose file and adds a lightweight `dev` workspace service.

## Troubleshooting

- **Slow startup or portal never loads** — raise the Podman/Docker Desktop VM to 8–10 GB (see Requirements) and recreate the stack.
- **Stuck waiting for db** — check `podman compose ps`; unhealthy MariaDB blocks `sakai`.
- **Port in use** — stop whatever is on `8080`, `8000`, or `53306`.
- **Empty portal on dev profile** — run `./docker/bin/deploy.sh` then `./docker/bin/tomcat.sh restart`.
- **DB init only on empty volume** — `podman compose down -v` to recreate.
- **Sakai cannot reach DB in Compose** — hostname must be `db`, not `localhost`.
- **`--profile full` and `--profile dev` together** — both bind `127.0.0.1:8080`; run only one.
