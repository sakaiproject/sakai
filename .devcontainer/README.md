# Developing Sakai with Dev Containers

This folder contains three ready-to-use [dev container](https://containers.dev/) configurations that give you a local Sakai environment (Tomcat + database) with nothing installed on your machine except Docker.

| Configuration | Folder | Database service | Sakai Maven profile |
|---|---|---|---|
| Sakai MariaDB Dev Container | `.devcontainer/mariadb/` | `mariadb:10` | none (default) |
| Sakai MySQL Dev Container | `.devcontainer/mysql/` | `mysql:8` | `-Pmysql` |
| Sakai Oracle Dev Container | `.devcontainer/oracle/` | `container-registry.oracle.com/database/free:latest` (`FREEPDB1`) | `-Poracle` |

Each configuration is self-contained: it starts the workspace container, provisions the database, and runs a post-create script that prepares your Tomcat instance and shell aliases.

## Requirements

- [Docker](https://docs.docker.com/get-docker/) running locally.
- [Visual Studio Code](https://code.visualstudio.com/) with the
  [Dev Containers extension](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers)
  (`ms-vscode-remote.remote-containers`).
- Nothing else: Java, Maven and Tomcat are installed inside the container.

Any other Dev Containers client (e.g. GitHub Codespaces, `devcontainer` CLI, JetBrains Gateway) works too, as long as it can read `devcontainer.json`.

## Quick start

1. Open the Sakai repository in VS Code.
2. Run **Dev Containers: Reopen in Container** from the Command Palette (`F1`).
3. Pick one of the three configurations:
   - `mariadb` — fastest option, uses the default Sakai build.
   - `mysql` — starts MySQL 8 and builds Sakai with the `mysql` Maven profile (MySQL JDBC connector).
   - `oracle` — starts Oracle Database Free; the first boot takes several minutes.
4. Wait for the container to build and for `setup.sh` to finish.
5. Build and deploy Sakai, then start Tomcat:

   ```bash
   mcid      # mvn clean install sakai:deploy-exploded -DskipTests=true (plus the profile of the variant)
   tstart    # catalina.sh start
   ```

6. Open <http://localhost:8080> and log in with the Sakai demo users (`-Dsakai.demo=true` is enabled in `setenv.sh`).

Stop Tomcat with `tstop`.

## What the container provides

Installed by the dev container features declared in each `devcontainer.json`:

- **JDK**: Eclipse Temurin `21.0.7-tem`
- **Apache Maven**: `3.9.16`
- **Apache Tomcat**: `10.1.55` (via SDKMAN, installed as the `tomcat` candidate)
- **opencode**: `1.18.28`

VS Code extensions installed by `customizations.vscode.extensions`:

| Variant | Extensions |
|---|---|
| `mariadb`, `mysql` | `sst-dev.opencode`, `mtxr.sqltools-driver-mysql` (SQL Tools driver for MySQL/MariaDB) |
| `oracle` | `sst-dev.opencode`, `Oracle.sql-developer` |

Defined by `docker-compose.yml` (same `devcontainer` service in all three variants, only the database service differs):

- **`devcontainer`**: `mcr.microsoft.com/devcontainers/base:jammy`, running as user `vscode`, started with `command: sleep infinity`.
  - Mounts the repository at `/workspaces/app` (`../../:/workspaces/app:cached`).
  - Persists the Maven local repository in the `maven-cache-vol` Docker volume, so `~/.m2` survives container rebuilds.
  - Reads its environment from the variant's `.env` file.
  - Joins the `backend-local` (database) and `pubnet-local` networks.
  - `depends_on: dblocal` — in the Oracle variant with `condition: service_healthy`.
  - **`dblocal`**: the database, published on the host with a `ports` mapping (no `forwardPorts` is declared in `devcontainer.json`):
    - MariaDB: `3306:3306`, image `mariadb:10`, `MARIADB_ROOT_PASSWORD=sakairoot`, data persisted in the `mariadb-data-vol` volume
    - MySQL: `3306:3306`, image `mysql:8`, `MYSQL_ROOT_PASSWORD=sakairoot`, data persisted in the `mysql-data-vol` volume
    - Oracle: `1521:1521`, service name `FREEPDB1`, data persisted in the `oradata-local-vol` volume, health check based on the `/tmp/ready` marker file (interval 60s, 10 retries, 30s grace period)

## Shell aliases

`setup.sh` appends the following aliases to `~/.bash_aliases` inside the container:

| Alias | Command | Description |
|---|---|---|
| `mcid` | `mvn clean install sakai:deploy-exploded -DskipTests=true` (`-Pmysql` / `-Poracle` on the other variants) | Build all of Sakai and deploy it exploded into the local Tomcat |
| `tstart` | `catalina.sh start` | Start Tomcat |
| `tstop` | `catalina.sh stop` | Stop Tomcat |
| `tlog` | `tail -f $CATALINA_BASE/logs/catalina.out` | Follow the Tomcat log |
| `olog` | `code $CATALINA_BASE/logs/catalina.out` | Open the Tomcat log in VS Code |
| `rlog` | `rm -rf $CATALINA_BASE/logs/*` | Delete all Tomcat logs |
| `tremove` | `rm -rf $CATALINA_BASE/lib $CATALINA_BASE/components $CATALINA_BASE/webapps` | Remove deployed Sakai artifacts before a clean redeploy |

Reopen the terminal (or the container) if the aliases are not available yet.

## Tomcat layout

`setup.sh` builds the Tomcat base directory (`$CATALINA_BASE`, i.e. `~/sakai-tomcat`) from symlinks into this folder:

```
~/sakai-tomcat/
├── bin     -> .devcontainer/tomcat/bin      (setenv.sh, entrypoint.sh)
├── conf    -> .devcontainer/tomcat/conf     (server.xml, web.xml, ...)
├── sakai   -> .devcontainer/tomcat/sakai-<variant>   (sakai.home: sakai.properties, content, ...)
└── logs    (real directory)
```

The `sakai` symlink targets `sakai-mariadb` for the MariaDB **and MySQL** variants (see [Known quirks](#known-quirks-in-the-mysql-variant)) and `sakai-oracle` for the Oracle variant.

Relevant details:

- HTTP connector listens on **8080** (`server.xml`), shutdown port `8005`.
- `bin/setenv.sh` configures the JVM: `-Xms2g -Xmx2g`, generational ZGC, `sakai.home` pointing at the `sakai` folder, demo mode enabled, timezone `Europe/Madrid`, encoding/locale `es_ES` and UTF-8.
- `sakai.properties` in each `sakai-<variant>` folder points to the database host `dblocal` (the compose service name), so never change it to `localhost` inside the container.

## Database bootstrap

Database initialization scripts live in `db/` and are executed automatically by the database image:

- **MariaDB / MySQL**: `sakai.sql` is mounted as `/docker-entrypoint-initdb.d` and runs on first start only (empty data directory). It creates the databases `sakai`, `sakai12`, `sakai19` and `sakai20`, and grants all privileges on `sakai%` to the user `sakai` (password `ironchef`) for `localhost`, `127.0.0.1` and any host.
  The root password comes from the compose environment: `MARIADB_ROOT_PASSWORD=sakairoot` (MariaDB) or `MYSQL_ROOT_PASSWORD=sakairoot` (MySQL).
- **Oracle**: `db/init-script.sh` is mounted as `/opt/oracle/scripts/startup`, so it runs on **every** start. It connects with `sqlplus` (`/opt/oracle/product/26ai/dbhomeFree/bin/sqlplus`) as `sys` into `FREEPDB1` and creates the Sakai user (`$ORACLE_USR` / `$ORACLE_PWD`, both taken from `.env`) with `USERS` tablespace quota plus `SESSION`, `TABLE`, `SEQUENCE`, `PROCEDURE` and `VIEW` grants. It then writes `/tmp/ready`, which is what the compose health check tests.
  Compose also sets `ORACLE_PDB=FREEPDB1` and `DBHOST=dblocal`; two commented-out `NLS_LANG` options are available for character-set tuning.

Database credentials used by Sakai (`username@javax.sql.BaseDataSource` / `password@javax.sql.BaseDataSource` in `sakai.properties`) are `sakai` / `ironchef`. All container environment variables come from the `.env` file in each variant folder — keep real secrets out of version control.

## Typical workflow

```bash
mcid       # build Sakai and deploy it (first run downloads a lot of dependencies)
tstart     # start Tomcat
tlog       # watch catalina.out until Sakai is up
# ... develop, then rebuild only what changed:
mcid && tstop && tstart
rlog       # clean logs when they get noisy
tremove    # wipe deployed artifacts before a full rebuild
```

Notes:

- The first `mcid` is slow; later runs are incremental and the Maven cache is kept in a Docker volume.
- Oracle's first start is the slowest of the three: the image has to create the database and run `init-script.sh` before the health check passes.
- To rebuild the container from scratch, use **Dev Containers: Rebuild Container**.
- To reset the database (delete all data and reinitialize), run `docker compose down -v` from the corresponding variant folder (`.devcontainer/mariadb`, `.devcontainer/mysql`, or `.devcontainer/oracle`). This will remove the container and its named data volume. Be careful: this is destructive and cannot be undone.

## Folder reference

```
.devcontainer/
├── README.md                 # this file
├── mariadb/                  # MariaDB variant
│   ├── devcontainer.json     # container definition, features, VS Code extensions, postCreateCommand
│   ├── docker-compose.yml    # devcontainer + dblocal (mariadb:10) services
│   ├── .env                  # environment for both services
│   ├── setup.sh              # post-create script (symlinks, aliases, fonts)
│   └── db/sakai.sql
├── mysql/                    # MySQL variant (same layout, dblocal = mysql:8)
├── oracle/                   # Oracle variant (dblocal = Oracle Database Free)
│   └── db/init-script.sh
└── tomcat/                   # shared Tomcat resources for all variants
    ├── bin/                  # setenv.sh, entrypoint.sh
    ├── conf/                 # server.xml, web.xml, catalina.properties, context.xml
    ├── sakai-mariadb/        # sakai.home for the MariaDB variant
    ├── sakai-mysql/          # sakai.home for the MySQL variant
    └── sakai-oracle/         # sakai.home for the Oracle variant
```

`devcontainer-lock.json` pins the feature versions used to build the image; keep it committed so builds stay reproducible.

## Known quirks in the MySQL variant

- `mysql/setup.sh` links `tomcat/sakai-mariadb` as `sakai.home` instead of `tomcat/sakai-mysql`, so `sakai-mysql/sakai.properties` is currently unused. The effective datasource config is the MariaDB one (`org.mariadb.jdbc.Driver`, `jdbc:mariadb://dblocal:3306/sakai`, `MariaDBDialect`), which is compatible with MySQL 8 even though `mcid` also activates `-Pmysql` and pulls `mysql-connector-j` into the classpath.
- `mysql/db/sakai.sql` uses the pre-8.0 `GRANT ... IDENTIFIED BY` syntax. MySQL 8 removed that clause, so those three `GRANT` statements can fail during first-time initialization and the `sakai` user may be missing. If that happens, create it manually:

  ```sql
  CREATE USER 'sakai'@'%' IDENTIFIED BY 'ironchef';
  GRANT ALL PRIVILEGES ON `sakai%`.* TO 'sakai'@'%';
  FLUSH PRIVILEGES;
  ```

## Troubleshooting

- **Aliases not found**: open a new terminal, or rebuild the container so `setup.sh` runs again.
- **Port already in use**: stop the local process using `8080` (Tomcat), `3306` (MariaDB/MySQL) or `1521` (Oracle), or change the host side of the `ports` mapping in `docker-compose.yml`.
- **Oracle health check fails**: the first boot can exceed the health check window; restart the container or increase `retries`/`start_period` in `oracle/docker-compose.yml`.
- **Stale or half-deployed Sakai**: run `tremove`, then `mcid && tstart`.
- **Lost Maven cache**: it lives in the `maven-cache-vol` volume; delete the volume only if you want to force a full dependency download.
- **Different locale/timezone**: adjust `-Duser.timezone`, `-Duser.language` and `-Duser.region` in `tomcat/bin/setenv.sh`.
