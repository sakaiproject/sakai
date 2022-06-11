package org.sakaiproject.modi;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

import static org.sakaiproject.modi.SysProp.*;

@Slf4j
/**
 * Handles the initial resolution of the Sakai home directory ("sakai.home").
 *
 * When calling {@link Environment#initialize()}, the system properties are read, and resolved
 * with the conventional defaults/fallback:
 *
 *   1. If sakai.home is set and writable, it is used
 *   2. If sakai.home is set, but does not exist, it is created and used
 *   3. If sakai.home is set, and is not a directory, or is not readable and writable, an exception is thrown
 *   4. If sakai.home is not set, the above three rules are applied for catalina.base
 *   5. If catalina.base is not set, the first three rules are applied for /tmp/sakai
 *
 * If a suitable directory is found, it is written back to sakai.home, with a trailing separator (slash).
 *
 * Note that ever attempting to run in /tmp/sakai is dubious. It is worth revisiting whether we should fail
 * before trying it.
 */
public final class Environment {
    /** Public construction is prohibited. We only allow entry through the {@link #initialize()} method. */
    private Environment() {
        ensureRequirements();
        this.catalinaBase = catalina_base.getRaw();
        this.sakaiHome = sakai_home.getRaw();
        this.componentsRoot = sakai_components_root.getRaw();
        this.sakaiSecurity = sakai_security.getRaw();
    }

    /**
     * Use or infer the sakai.home value, and then check/create the directory.
     * <p>
     * When done, we set the System property again to ensure that it has a trailing
     * slash because many places use it for bare concatenation.
     *
     * @return
     * @throws CouldNotCreateSakaiHomeException    if the directory cannot be established/created
     * @throws CouldNotReadWriteSakaiHomeException if the directory cannot be read/written
     */
    public static Environment initialize() throws CouldNotCreateSakaiHomeException, CouldNotReadWriteSakaiHomeException {
        Environment env = new Environment();
        return env;
    }

    /** The main workflow method for setup. */
    private void ensureRequirements() {
        ensureCatalinaBase();
        ensureSakaiHome();
        ensureComponents();
        ensureSecurity();
    }

    /** The Tomcat/Catalina base directory, catalina.base */
    @Getter private final String catalinaBase;
    /** The main Sakai Home directory, sakai.home -- usually ${catalina.base}/sakai */
    @Getter private final String sakaiHome;
    /** The directory for traditional on-disk components, sakai.components.root -- usually ${catalina.base}/components */
    @Getter private final String componentsRoot;
    /** The optional directory for properties files sensitive data, sakai.security -- usually null/empty */
    @Getter private final String sakaiSecurity;

    private void ensureSakaiHome() {
        Path home = computedHomePath();
        createHomeIfNeeded(home);
        checkHomeReadWrite(home);
        sakai_home.set(withTrailingSlash(home));
    }

    private void ensureCatalinaBase() {
        catalina_base.getPath()
                .filter(Files::isDirectory)
                .filter(Files::isReadable)
                .orElseThrow(this::catalinaBaseMissing);
    }

    private void ensureComponents() {
        computedComponentsRoot()
                .filter(Files::isDirectory)
                .filter(Files::isReadable)
                .ifPresentOrElse(sakai_components_root::set, () -> { throw componentsUnreadable(); });
    }

    private void ensureSecurity() {
        Path path = sakai_security.getRawPath();
        if (path == null) return;
        if (!(Files.isDirectory(path) && Files.isReadable(path))) throw securityUnreable();

        sakai_security.set(path);
    }

    /** The path to use, as computed by property fallthrough. */
    private Path computedHomePath() {
        return sakai_home.getPath()
                .or(this::defaultHomePath)
                .orElse(Path.of("/tmp/sakai"));
    }

    private Optional<Path> computedComponentsRoot() {
        return sakai_components_root.getPath().or(this::defaultComponentsPath);
    }

    private Optional<Path> defaultComponentsPath() {
        return catalina_base.getPathPlus("components");
    }

    /** Create the computed directory, if needed */
    private void createHomeIfNeeded(Path path) throws CouldNotCreateSakaiHomeException {
        try {
            if (!Files.isDirectory(path))
                Files.createDirectory(path);
            log.info("Created Sakai home directory (sakai.home) at: {}", path);
        } catch (IOException e) {
            throw new CouldNotCreateSakaiHomeException(path);
        }
    }

    /** Ensure a readable/writable directory. */
    private void checkHomeReadWrite(Path path) throws CouldNotReadWriteSakaiHomeException {
        if (!(Files.isDirectory(path) && Files.isReadable(path) && Files.isWritable(path)))
            throw new CouldNotReadWriteSakaiHomeException(path);
    }

    /** The optional path as set in system properties (generally with JAVA_OPTS or CATALINA_OPTS). */
    private Optional<Path> configuredHomePath() {
        return sakai_home.getPath();
    }

    /** The default path if no value is set; the "sakai/" directory within Tomcat's base directory. */
    private Optional<Path> defaultHomePath() {
        return catalina_base.get()
                .map(Path::of)
                .map(p -> p.resolve("sakai"));
    }

    private String withTrailingSlash(Path path) {
        return withTrailingSlash(path.toString());
    }

    private String withTrailingSlash(String path) {
        return path.endsWith(File.separator)
                ? path
                : path + "/";
    }

    private static final String MISSING_MESSAGE =
            "Cannot finish initialization; {} is missing or unreadable.\n"
                    + "    It is set to: '{}'\n"
                    + "    Check your Tomcat configuration.";

    private String format(String msg, Object... args) {
        return MessageFormatter.arrayFormat(msg, Arrays.stream(args).toArray()).getMessage();
    }

    private InitializationException fatalError(String msg, Object... args) {
        return new InitializationException(format(msg, args));
    }

    private InitializationException catalinaBaseMissing() {
        return fatalError(MISSING_MESSAGE, "catalina.base", catalina_base.getRaw());
    }

    private InitializationException componentsUnreadable() {
        return fatalError(MISSING_MESSAGE, "sakai.components.root", catalina_base.getRaw());
    }

    private InitializationException securityUnreable() {
        return fatalError(MISSING_MESSAGE, "sakai.security", sakai_security.getRaw());
    }
}