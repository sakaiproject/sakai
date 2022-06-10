package org.sakaiproject.modi;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Slf4j
/**
 * Handles the initial resolution of the Sakai home directory ("sakai.home").
 *
 * When calling {@link SakaiHome#ensure()}, the system properties are read, and resolved
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
public final class SakaiHome {
    /** Public construction is prohibited. We only allow entry through the {@link #ensure()} method. */
    private SakaiHome() {}

    /**
     * Use or infer the sakai.home value, and then check/create the directory.
     *
     * When done, we set the System property again to ensure that it has a trailing
     * slash because many places use it for bare concatenation.
     *
     * @throws CouldNotCreateSakaiHomeException if the directory cannot be established/created
     * @throws CouldNotReadWriteSakaiHomeException if the directory cannot be read/written
     */
    public static void ensure() throws CouldNotCreateSakaiHomeException, CouldNotReadWriteSakaiHomeException {
        new SakaiHome().ensureSetup();
    }

    /** The main workflow method for setup. */
    private void ensureSetup() {
        Path home = computedPath();
        createIfNeeded(home);
        checkReadWrite(home);
        SysProp.sakaiHome.setValue(home.toAbsolutePath() + "/");
    }

    /** The path to use, as computed by property fallthrough. */
    private Path computedPath() {
        return configuredPath()
                .or(this::defaultPath)
                .orElseGet(this::fallbackPath);
    }

    /** Create the computed directory, if needed */
    protected void createIfNeeded(Path path) throws CouldNotCreateSakaiHomeException {
        try {
            if (!Files.isDirectory(path))
                Files.createDirectory(path);
            log.info("Created Sakai home directory (sakai.home) at: {}", path);
        } catch (IOException e) {
            throw new CouldNotCreateSakaiHomeException(path);
        }
    }

    /** Ensure a readable/writable directory. */
    protected void checkReadWrite(Path path) throws CouldNotReadWriteSakaiHomeException {
        if (!(Files.isDirectory(path) && Files.isReadable(path) && Files.isWritable(path)))
            throw new CouldNotReadWriteSakaiHomeException(path);
    }

    /** The optional path as set in system properties (generally with JAVA_OPTS or CATALINA_OPTS). */
    protected Optional<Path> configuredPath() {
        return SysProp.sakaiHome.getValue().map(Path::of);
    }

    /** The default path if no value is set; the "sakai/" directory within Tomcat's base directory. */
    protected Optional<Path> defaultPath() {
        return SysProp.catalinaBase.getValue()
                .map(Path::of)
                .map(p -> p.resolve("sakai"));
    }

    /** The fallback path in temporary space, in case all else fails, to attempt startup. */
    protected Path fallbackPath() {
        return Path.of("/tmp/sakai");
    }
}