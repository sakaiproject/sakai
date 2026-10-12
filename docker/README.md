# Sakai Docker / Compose

Compose-first local development for Sakai on **Java 21** and **Tomcat 10.1**.

The Compose file lives at the **repository root** ([`docker-compose.yml`](../docker-compose.yml)) so the build context is `.` (this checkout), not `docker/..`.

## Requirements

- [Docker](https://docs.docker.com/get-docker/) or [Podman](https://podman.io/) with Compose v2 (`docker compose` / `podman compose`)
- For the **dev** profile: **JDK 21** and Maven 3.9+ on the host (or `./mvnw`)

## Quick start (full image)

From the repository root:

```bash
podman compose build app          # produces image sakai:local (not sakai-sakai)
podman compose up -d              # detached — avoids a “hung” attached CLI during boot
podman compose logs -f app        # wait for “Server startup in …”
# or the same with docker compose
```

Attached `up --build` is fine but looks frozen for a long time (full Maven reactor + Tomcat boot). Prefer `build` then `up -d` + `logs -f`.

This starts MariaDB and the Sakai image from [`Dockerfile`](Dockerfile). Open http://localhost:8080/portal when startup finishes.
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
| `app` | (default) | image `sakai:local` via [`Dockerfile`](Dockerfile) | `8080` | Built from this checkout |
| `tomcat` | `dev` | `tomcat:10.1-jdk21-temurin` | `8080`, `8000` | Mounts `.docker-deploy/{webapps,components,lib}` |

Helpers: [`bin/deploy.sh`](bin/deploy.sh), [`bin/tomcat.sh`](bin/tomcat.sh).

## JDBC hosts

| Where Tomcat runs | JDBC host |
|-------------------|-----------|
| Compose `app` / `tomcat` | `db` ([`tomcat/sakai/sakai.properties`](tomcat/sakai/sakai.properties)) |
| Host process + Compose `db` | `127.0.0.1:53306` ([host-example](tomcat/sakai/sakai.properties.host-example)) |

## Dockerfile

Multi-stage build used by the default `sakai` service:

1. **Build** — `maven:3.9-eclipse-temurin-21`, `sakai:deploy-exploded` into `/deploy`
2. **Runtime** — `tomcat:10.1-jdk21-temurin` with conf from `docker/tomcat/`

`COPY docker/...` paths are relative to the **repo root** build context (not because Compose is nested under `docker/`).

### Build memory

Defaults for `MAVEN_OPTS` and `MAVEN_THREADS` are defined only in [`Dockerfile`](Dockerfile) (not duplicated in Compose). Maven uses ~75% of the build container’s RAM and caps parallel modules at 2.

If you still get `Java heap space`:

1. Give the Podman machine / Docker Desktop VM more RAM (8g+ is typical for a full Sakai build).
2. Rebuild with a fixed heap, for example:

```bash
podman compose build --build-arg MAVEN_OPTS="-Xms2g -Xmx8g -XX:+TieredCompilation -XX:TieredStopAtLevel=1" sakai
```

## Dev Containers

[`.devcontainer/`](../.devcontainer/) reuses the root Compose file and adds a lightweight `dev` workspace service.

## Troubleshooting

- **Everything looks hung** — full image build is slow; use `podman compose build app` then `up -d` and `logs -f app`. If stuck on “waiting for db”, check `podman compose ps` — unhealthy MariaDB blocks `app` forever.
- **Image named `sakai-sakai`** — old default; current Compose tags **`sakai:local`**. Remove the old one if you like: `podman rmi docker.io/library/sakai-sakai`.
- **Port in use** — stop whatever is on `8080`, `8000`, or `53306`.
- **Empty portal on dev profile** — run `./docker/bin/deploy.sh` then `./docker/bin/tomcat.sh restart`.
- **DB init only on empty volume** — `podman compose down -v` to recreate.
- **Sakai cannot reach DB in Compose** — hostname must be `db`, not `localhost`.
- **Do not run default `app` and `--profile dev` together** — both bind host port `8080`.
- **Leftover containers/networks** — clean up once, then retry:

```bash
podman compose down --remove-orphans
podman rm -f sakai-db sakai-tomcat sakai-tomcat-dev 2>/dev/null || true
podman network rm sakai-local 2>/dev/null || true
podman compose build app && podman compose up -d && podman compose logs -f app
```
