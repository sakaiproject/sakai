/*
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/** Shared path policy for untrusted ZIP archives and content-package references. */
public final class Zip {
    private Zip() {
    }

    /**
     * Returns a portable relative entry name suitable for a filesystem or a content collection.
     * Rejects absolute paths, parent traversal, Windows drive/stream names, and empty names.
     * Backslashes are treated as separators; redundant separators and dot segments are removed.
     * A trailing separator is retained for directory entries.
     */
    public static String validateEntryName(String name) throws InvalidEntryException {
        String portableName = relativeName(name);
        List<String> parts = new ArrayList<>();
        for (String part : portableName.split("/")) {
            if ("..".equals(part) || part.indexOf(':') >= 0) {
                throw new InvalidEntryException("Unsafe path in ZIP archive");
            }
            if (!part.isEmpty() && !".".equals(part)) {
                parts.add(part);
            }
        }
        if (parts.isEmpty()) {
            throw new InvalidEntryException("Empty path in ZIP archive");
        }
        return String.join("/", parts) + (portableName.endsWith("/") ? "/" : "");
    }

    /**
     * Resolves an archive entry under an existing extraction directory using canonical containment.
     * Callers must use a private directory that untrusted processes cannot modify during extraction.
     * Validation happens before callers create directories or open output streams.
     */
    public static Path resolveEntryPath(Path directory, String entryName) throws IOException {
        return resolveContainedPath(directory, validateEntryName(entryName));
    }

    /**
     * Resolves a package reference under an existing directory, including symlink containment.
     * Unlike archive entry names, references may use parent segments provided they stay inside
     * the package (for example, pages/../images in a package reference).
     */
    public static Path resolveContainedPath(Path directory, String name) throws IOException {
        String portableName = relativeName(name);
        Path root = directory.toRealPath();
        try {
            Path destination = root;
            for (Path component : root.getFileSystem().getPath(portableName)) {
                if (".".equals(component.toString())) {
                    continue;
                }
                if ("..".equals(component.toString())) {
                    destination = destination.getParent();
                } else {
                    destination = destination.resolve(component);
                    boolean exists;
                    try {
                        // Inspect the link itself so a dangling link cannot look like a new file.
                        Files.readAttributes(destination, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                        exists = true;
                    } catch (NoSuchFileException e) {
                        exists = false;
                    }
                    if (exists) {
                        // Resolve existing ancestors even when the final output file is not created yet.
                        destination = destination.toRealPath();
                    }
                }
                if (destination == null || !destination.startsWith(root)) {
                    throw new InvalidEntryException("Path escapes ZIP archive directory");
                }
            }
            if (destination.equals(root)) {
                throw new InvalidEntryException("Path escapes ZIP archive directory");
            }
            return destination;
        } catch (InvalidPathException e) {
            throw new IOException("Invalid path in ZIP archive", e);
        }
    }

    /** Signals a security rejection; callers must not retry it as a charset or parsing failure. */
    public static final class InvalidEntryException extends IOException {
        private static final long serialVersionUID = 1L;

        private InvalidEntryException(String message) {
            super(message);
        }
    }

    private static String relativeName(String name) throws InvalidEntryException {
        if (name == null || name.isBlank() || name.indexOf('\0') >= 0) {
            throw new InvalidEntryException("Empty or invalid path in ZIP archive");
        }
        String portableName = name.replace('\\', '/');
        if (portableName.startsWith("/") || (portableName.length() > 1 && portableName.charAt(1) == ':')) {
            throw new InvalidEntryException("Absolute path in ZIP archive");
        }
        return portableName;
    }
}
