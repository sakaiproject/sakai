package coza.opencollab.sakai.cloudcontent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.FileSystemHandler;

import io.minio.GetObjectArgs;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * MinIO-backed implementation of {@link FileSystemHandler} using the legacy
 * BlobStore configuration keys for drop-in compatibility with the former
 * jclouds handler.
 */
@Setter
@Slf4j
public class BlobStoreFileSystemHandler implements FileSystemHandler {

    private ServerConfigurationService serverConfigurationService;

    private String endpoint;
    // Retain legacy property names: identity/credential instead of access/secret key
    private String identity;
    private String credential;
    private String baseContainer;
    private boolean useIdForPath = false;
    private String invalidCharactersRegex = "[:*?<|>]";

    private MinioClient client;

    private long maxBlobStreamSize;
    private int partSize;
    private int signedUrlExpiry;

    // Allow new-style property names for completeness
    public void setAccessKey(String accessKey) {
        this.identity = accessKey;
    }

    public void setSecretKey(String secretKey) {
        this.credential = secretKey;
    }

    public void init() {
        // Back-compat: if endpoint not set, try derive from legacy provider key
        if (endpoint == null || endpoint.trim().isEmpty()) {
            String derived = resolveEndpointFromLegacyProvider();
            if (derived != null) {
                endpoint = derived;
                log.info("Derived S3 endpoint '{}' from legacy provider=aws-s3; consider setting endpoint@{} explicitly.", endpoint, LEGACY_BEAN_ID);
            }
        }
        // Validate required credentials before constructing the client
        if (endpoint == null || endpoint.trim().isEmpty()) {
            String msg = "Missing required configuration 'endpoint' for bean 'org.sakaiproject.content.api.FileSystemHandler.blobstore' (key: endpoint@org.sakaiproject.content.api.FileSystemHandler.blobstore)";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (identity == null || identity.trim().isEmpty()) {
            String msg = "Missing required configuration 'identity' (aka accessKey) for bean 'org.sakaiproject.content.api.FileSystemHandler.blobstore' (key: identity@org.sakaiproject.content.api.FileSystemHandler.blobstore)";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (credential == null || credential.trim().isEmpty()) {
            String msg = "Missing required configuration 'credential' (aka secretKey) for bean 'org.sakaiproject.content.api.FileSystemHandler.blobstore' (key: credential@org.sakaiproject.content.api.FileSystemHandler.blobstore)";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Validate numeric configuration with sensible defaults / fail fast
        long defaultMaxBlobStreamSize = 104857600L; // 100 MiB
        long configuredMaxBlobStreamSize = serverConfigurationService.getLong("cloud.content.maxblobstream.size", defaultMaxBlobStreamSize);
        if (configuredMaxBlobStreamSize <= 0) {
            String msg = "Invalid 'cloud.content.maxblobstream.size' (" + configuredMaxBlobStreamSize + "): must be a positive integer";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (configuredMaxBlobStreamSize > Integer.MAX_VALUE) {
            log.warn("'cloud.content.maxblobstream.size' ({}) exceeds Integer.MAX_VALUE; capping to {} bytes.", configuredMaxBlobStreamSize, Integer.MAX_VALUE);
            configuredMaxBlobStreamSize = Integer.MAX_VALUE;
        }
        this.maxBlobStreamSize = configuredMaxBlobStreamSize;

        int configuredSignedUrlExpiry = serverConfigurationService.getInt("cloud.content.signedurl.expiry", 600);
        if (configuredSignedUrlExpiry <= 0) {
            String msg = "Invalid 'cloud.content.signedurl.expiry' (" + configuredSignedUrlExpiry + "): must be a positive integer (seconds)";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        this.signedUrlExpiry = configuredSignedUrlExpiry;

        int configuredPartSizeMb = serverConfigurationService.getInt("cloud.content.multipart.partsize.mb", 10);
        if (configuredPartSizeMb <= 0) {
            String msg = "Invalid 'cloud.content.multipart.partsize.mb' (" + configuredPartSizeMb + "): must be a positive integer (MiB)";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        // Enforce minimum 5 MiB per S3/MinIO multipart rules and compute safely
        long partSizeBytes = Math.max(5, configuredPartSizeMb) * 1024L * 1024L;
        if (partSizeBytes > Integer.MAX_VALUE) {
            // Cap to Integer.MAX_VALUE to satisfy MinIO client API (int)
            log.warn("'cloud.content.multipart.partsize.mb' too large ({} MiB). Capping to {} bytes.", configuredPartSizeMb, Integer.MAX_VALUE);
            this.partSize = Integer.MAX_VALUE;
        } else {
            this.partSize = (int) partSizeBytes;
        }

        // All checks passed; construct the MinIO client last
        this.client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(identity, credential)
                .build();
    }

    private static final String LEGACY_BEAN_ID = "org.sakaiproject.content.api.FileSystemHandler.blobstore";

    /**
     * Legacy compatibility: if old jclouds config used provider=aws-s3 without an explicit endpoint,
     * infer a reasonable AWS S3 endpoint (optionally using region@... if present).
     * Returns null if no inference can be made.
     */
    private String resolveEndpointFromLegacyProvider() {
        try {
            String provider = serverConfigurationService.getString("provider@" + LEGACY_BEAN_ID);
            if (provider != null) provider = provider.trim();
            if (provider == null || provider.isEmpty()) {
                return null;
            }
            if ("aws-s3".equalsIgnoreCase(provider) || "s3".equalsIgnoreCase(provider)) {
                String region = serverConfigurationService.getString("region@" + LEGACY_BEAN_ID);
                region = (region == null ? null : region.trim());
                if (region == null || region.isEmpty()) {
                    return "https://s3.amazonaws.com";
                } else {
                    return "https://s3." + region + ".amazonaws.com";
                }
            }
        } catch (RuntimeException e) {
            log.warn("Unable to resolve endpoint from legacy provider setting", e);
        }
        return null;
    }

    public void destroy() {
        // nothing to destroy
    }

    @Override
    public URI getAssetDirectLink(String id, String root, String filePath) throws IOException {
        ContainerAndName can = getContainerAndName(id, root, filePath);
        try {
            String url = client.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(can.container)
                            .object(can.name)
                            .method(Method.GET)
                            .expiry(signedUrlExpiry, TimeUnit.SECONDS)
                            .build());
            return URI.create(url);
        } catch (Exception e) {
            throw new IOException("Unable to get presigned URL", e);
        }
    }

    @Override
    public InputStream getInputStream(String id, String root, String filePath) throws IOException {
        ContainerAndName can = getContainerAndName(id, root, filePath);
        try {
            // Always drain/close the underlying HTTP response because Spring may never read it
            // (e.g. when returning 304 from a cached request), leading OkHttp to log leaked bodies.
            try (InputStream responseStream = client.getObject(
                    GetObjectArgs.builder().bucket(can.container).object(can.name).build())) {
                // Accumulate in memory until we overshoot the threshold; only then spill to disk.
                int initial = (int) Math.min(maxBlobStreamSize, 4L * 1024 * 1024); // up to 4 MiB
                ByteArrayOutputStream memory = new ByteArrayOutputStream(initial);
                File tmp = null;
                FileOutputStream fos = null;
                boolean spooledToDisk = false;
                byte[] buffer = new byte[8192];
                int read;
                try {
                    while ((read = responseStream.read(buffer)) != -1) {
                        if (!spooledToDisk && ((long) memory.size() + read > maxBlobStreamSize)) {
                            spooledToDisk = true;
                            tmp = File.createTempFile("minio", ".tmp");
                            fos = new FileOutputStream(tmp);
                            // On the first overflow, flush everything buffered so far to disk.
                            memory.writeTo(fos);
                            memory = null; // allow GC to reclaim the in-memory buffer early
                        }
                        if (spooledToDisk) {
                            fos.write(buffer, 0, read);
                        } else {
                            memory.write(buffer, 0, read);
                        }
                    }
                } catch (IOException e) {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException close) {
                            e.addSuppressed(close);
                        }
                    }
                    if (tmp != null && tmp.exists() && !tmp.delete()) {
                        log.warn("Failed to delete temporary blob file {}", tmp.getAbsolutePath());
                    }
                    throw e;
                }

                if (spooledToDisk) {
                    try {
                        fos.close();
                    } catch (IOException close) {
                        if (tmp != null && tmp.exists() && !tmp.delete()) {
                            log.warn("Failed to delete temporary blob file {}", tmp.getAbsolutePath());
                        }
                        throw close;
                    }
                    final File toDelete = tmp;
                    final FileInputStream fis;
                    try {
                        fis = new FileInputStream(toDelete);
                    } catch (IOException openEx) {
                        try {
                            if (toDelete.exists() && !toDelete.delete()) {
                                log.warn("Failed to delete temporary blob file {}", toDelete.getAbsolutePath());
                            }
                        } catch (Exception delEx) {
                            log.warn("Error deleting temporary blob file {}", toDelete.getAbsolutePath(), delEx);
                        }
                        throw openEx;
                    }
                    return new FilterInputStream(fis) {
                        @Override
                        public void close() throws IOException {
                            IOException closeEx = null;
                            try {
                                super.close();
                            } catch (IOException e) {
                                closeEx = e;
                            } finally {
                                if (!toDelete.delete() && toDelete.exists()) {
                                    log.warn("Failed to delete temporary blob file {}", toDelete.getAbsolutePath());
                                }
                                if (closeEx != null) {
                                    throw closeEx;
                                }
                            }
                        }
                    };
                } else {
                    // Smaller payloads stay in-memory so downstream code can reread safely.
                    return new ByteArrayInputStream(memory.toByteArray());
                }
            }
        } catch (Exception e) {
            throw new IOException("Unable to read object", e);
        }
    }

    @Override
    public long saveInputStream(String id, String root, String filePath, InputStream stream) throws IOException {
        if (stream == null) {
            return 0L;
        }

        ContainerAndName can = getContainerAndName(id, root, filePath);
        createContainerIfNotExist(can.container);

        try (CountingInputStream cis = new CountingInputStream(stream)) {
            Map<String, String> headers = new HashMap<>();
            headers.put("x-amz-meta-id", Base64.encodeBase64String(id.getBytes(StandardCharsets.UTF_8)));
            headers.put("x-amz-meta-path", filePath);

            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(can.container)
                            .object(can.name)
                            .stream(cis, -1, partSize)
                            .headers(headers)
                            .build());

            return cis.getCount();
        } catch (Exception e) {
            throw new IOException("Unable to save object", e);
        }
    }

    @Override
    public boolean delete(String id, String root, String filePath) {
        ContainerAndName can = getContainerAndName(id, root, filePath);
        try {
            client.removeObject(
                    RemoveObjectArgs.builder().bucket(can.container).object(can.name).build());
            return true;
        } catch (Exception e) {
            log.warn("Failed to delete object {}/{}: {}", can.container, can.name, e.getMessage(), e);
            return false;
        }
    }

    private void createContainerIfNotExist(String container) throws IOException {
        try {
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(container).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(container).build());
            }
        } catch (Exception e) {
            throw new IOException("Unable to ensure bucket exists: " + container, e);
        }
    }

    private ContainerAndName getContainerAndName(String id, String root, String filePath) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("The id cannot be null or empty!");
        }
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("The path cannot be null or empty!");
        }

        String path = (useIdForPath ? id : filePath);
        path = (root == null ? "" : root) + (path.startsWith("/") ? "" : "/") + path;
        path = path.replace("///", "/");
        path = path.replace("//", "/");
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        path = path.replaceAll(invalidCharactersRegex, "");

        ContainerAndName can = new ContainerAndName();
        if (baseContainer == null) {
            int index = path.indexOf("/");
            if (index == -1) {
                throw new IllegalArgumentException("No base container set. The path (" + path + ") must include a container and filename!");
            } else {
                can.container = path.substring(0, index);
                can.name = path.substring(index + 1);
            }
        } else {
            can.container = baseContainer;
            can.name = path;
        }
        return can;
    }

    /**
     * Simple counting InputStream.
     */
    private static class CountingInputStream extends FilterInputStream {
        private long count = 0;

        protected CountingInputStream(InputStream in) {
            super(in);
        }

        @Override
        public int read() throws IOException {
            int b = in.read();
            if (b != -1) {
                count++;
            }
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int n = in.read(b, off, len);
            if (n != -1) {
                count += n;
            }
            return n;
        }

        public long getCount() {
            return count;
        }
    }

    private static class ContainerAndName {
        String container;
        String name;
    }

}
