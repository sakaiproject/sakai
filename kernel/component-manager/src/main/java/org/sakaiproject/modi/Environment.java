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

/**
 * Handles the essential filename and directory conventions needed for starting up Sakai. This
 * includes initial resolution of the Sakai home directory ("sakai.home"). The rest of the
 * runtime configuration is handled elsewhere (e.g., {@link org.sakaiproject.util.SakaiProperties}
 * and {@link org.sakaiproject.component.api.ServerConfigurationService}.
 *
 * The Environment makes these fundamental values available for read, rather than requiring other
 * classes to use the string keys and system properties while running. That is fine for an extensible
 * configuration system, but problematic for what should be fundamental constants for a given launch.
 * Paths to files and directories are exposed as {@link Path} objects, rather than bare Strings, giving
 * a better API with less tedious string manipulation.
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
@Slf4j
public final class Environment {
    /** Public construction is prohibited. We only allow entry through the {@link #initialize()} method. */
    private Environment() {
        ensureRequirements();
        this.catalinaBase = catalina_base.getRawPath();
        this.sakaiHome = sakai_home.getRawPath();
        this.componentsRoot = sakai_components_root.getRawPath();
        this.sakaiSecurity = sakai_security.getRawPath();
        this.configurationFile = sakai_home.getRawPathPlus("sakai-configuration.xml");
        this.overridesFolder = sakai_home.getRawPathPlus("override");
    }

    /** The Tomcat/Catalina base directory, catalina.base */
    @Getter private final Path catalinaBase;

    /** The main Sakai Home directory, sakai.home -- usually ${catalina.base}/sakai */
    @Getter private final Path sakaiHome;

    /** The directory for traditional on-disk components, sakai.components.root -- usually ${catalina.base}/components */
    @Getter private final Path componentsRoot;

    /** The optional directory for properties files sensitive data, sakai.security -- usually null/empty */
    @Getter private final Path sakaiSecurity;

    /** The main configuration file (Spring file) for startup, ${sakai.home}/sakai-configuration.xml */
    @Getter private final Path configurationFile;

    /** The directory where component-specific Spring overrides can be, ${sakai.home}/override */
    @Getter private final Path overridesFolder;

    /**
     * Set up the required base operating environment for starting Sakai.
     *
     * Uses or infers the sakai.home value, and then checks/creates the directory.
     *
     * When done, we set the System property again to ensure that it has a trailing slash because
     * many places use it for bare concatenation.
     *
     * @return an initialized environment; see the getters for what is considered core for startup
     * @throws InitializationException if the directory cannot be created/read/written
     */
    public static Environment initialize() throws InitializationException {
        return new Environment();
    }

    /** The main workflow method for setup. */
    private void ensureRequirements() {
        ensureCatalinaBase();
        ensureSakaiHome();
        ensureComponents();
        ensureSecurity();
    }


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
    private void createHomeIfNeeded(Path path) throws InitializationException {
        try {
            if (!Files.isDirectory(path))
                Files.createDirectory(path);
            log.info("Created Sakai home directory (sakai.home) at: {}", path);
        } catch (IOException e) {
            throw couldNotCreateSakaiHome();
        }
    }

    /** Ensure a readable/writable directory. */
    private void checkHomeReadWrite(Path path) throws InitializationException {
        if (!(Files.isDirectory(path) && Files.isReadable(path) && Files.isWritable(path)))
            throw couldNotReadWriteSakaiHome();
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
                : path + File.separator;
    }

    private static final String COULD_NOT_CREATE =
            "Cannot finish initialization; could not create {}.\n"
                    + "    It is set to: '{}'\n"
                    + "    Check your Tomcat configuration and environment variables.";

    private static final String COULD_NOT_FIND =
            "Cannot finish initialization; {} is missing or unreadable.\n"
                    + "    It is set to: '{}'\n"
                    + "    Check your Tomcat configuration and environment variables.";

    private String format(String msg, Object... args) {
        return MessageFormatter.arrayFormat(msg, Arrays.stream(args).toArray()).getMessage();
    }

    private InitializationException fatalError(String msg, Object... args) {
        return new InitializationException(format(msg, args));
    }

    private InitializationException couldNotCreateSakaiHome() {
        return fatalError(COULD_NOT_CREATE, "sakai.home", sakai_home.getRaw());
    }

    private InitializationException couldNotReadWriteSakaiHome() {
        return fatalError(COULD_NOT_FIND, "sakai.home", sakai_home.getRaw());
    }

    private InitializationException catalinaBaseMissing() {
        return fatalError(COULD_NOT_FIND, "catalina.base", catalina_base.getRaw());
    }

    private InitializationException componentsUnreadable() {
        return fatalError(COULD_NOT_FIND, "sakai.components.root", catalina_base.getRaw());
    }

    private InitializationException securityUnreable() {
        return fatalError(COULD_NOT_FIND, "sakai.security", sakai_security.getRaw());
    }
}