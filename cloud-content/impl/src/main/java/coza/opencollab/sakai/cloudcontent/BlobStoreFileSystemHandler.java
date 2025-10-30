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

import io.minio.BucketExistsArgs;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.Directive;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import io.minio.messages.ErrorResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * MinIO-backed implementation of {@link FileSystemHandler}. This class replaces the
 * legacy jclouds handler but retains the same Spring bean id for compatibility.
 */
@Setter
@Slf4j
public class BlobStoreFileSystemHandler implements FileSystemHandler {

    private static final String LEGACY_BEAN_ID = "org.sakaiproject.content.api.FileSystemHandler.blobstore";

    private ServerConfigurationService serverConfigurationService;

    private String endpoint;
    private String identity;
    private String credential;
    private String baseContainer;
    private boolean useIdForPath = false;
    private String invalidCharactersRegex = "[:*?<|>]";

    private MinioClient client;

    private long maxBlobStreamSize;
    private int partSize;
    private int signedUrlExpiry;

    /**
     * Legacy synonym to ease property migrations from pre-MinIO configuration.
     */
    public void setAccessKey(String accessKey) {
        this.identity = accessKey;
    }

    /**
     * Legacy synonym to ease property migrations from pre-MinIO configuration.
     */
    public void setSecretKey(String secretKey) {
        this.credential = secretKey;
    }

    public void init() {
        if (serverConfigurationService == null) {
            throw new IllegalStateException("ServerConfigurationService must be provided");
        }

        if (endpoint == null || endpoint.trim().isEmpty()) {
            String derived = resolveEndpointFromLegacyProvider();
            if (derived != null) {
                endpoint = derived;
                log.info("Derived S3 endpoint '{}' from legacy provider configuration; please set endpoint@{} explicitly.",
                        endpoint, LEGACY_BEAN_ID);
            }
        }
        if (endpoint == null || endpoint.trim().isEmpty()) {
            String msg = "Missing required configuration 'endpoint' for bean '" + LEGACY_BEAN_ID
                    + "' (key: endpoint@" + LEGACY_BEAN_ID + ")";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (identity == null || identity.trim().isEmpty()) {
            String msg = "Missing required configuration 'identity' (aka accessKey) for bean '" + LEGACY_BEAN_ID
                    + "' (key: identity@" + LEGACY_BEAN_ID + ")";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (credential == null || credential.trim().isEmpty()) {
            String msg = "Missing required configuration 'credential' (aka secretKey) for bean '" + LEGACY_BEAN_ID
                    + "' (key: credential@" + LEGACY_BEAN_ID + ")";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }

        long defaultMaxBlobStreamSize = 104857600L; // 100 MiB
        long configuredMaxBlobStreamSize = serverConfigurationService.getLong("cloud.content.maxblobstream.size", defaultMaxBlobStreamSize);
        if (configuredMaxBlobStreamSize <= 0) {
            String msg = "Invalid 'cloud.content.maxblobstream.size' (" + configuredMaxBlobStreamSize + "): must be a positive integer";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (configuredMaxBlobStreamSize > Integer.MAX_VALUE) {
            log.warn("'cloud.content.maxblobstream.size' ({}) exceeds Integer.MAX_VALUE; capping to {} bytes.",
                    configuredMaxBlobStreamSize, Integer.MAX_VALUE);
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
        long partSizeBytes = Math.max(5, configuredPartSizeMb) * 1024L * 1024L;
        if (partSizeBytes > Integer.MAX_VALUE) {
            log.warn("'cloud.content.multipart.partsize.mb' too large ({} MiB). Capping to {} bytes.",
                    configuredPartSizeMb, Integer.MAX_VALUE);
            this.partSize = Integer.MAX_VALUE;
        } else {
            this.partSize = (int) partSizeBytes;
        }

        this.client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(identity, credential)
                .build();
    }

    private String resolveEndpointFromLegacyProvider() {
        try {
            String provider = serverConfigurationService.getString("provider@" + LEGACY_BEAN_ID);
            if (provider != null) {
                provider = provider.trim();
            }
            if (provider == null || provider.isEmpty()) {
                return null;
            }
            if ("aws-s3".equalsIgnoreCase(provider) || "s3".equalsIgnoreCase(provider)) {
                String region = serverConfigurationService.getString("region@" + LEGACY_BEAN_ID);
                if (region != null) {
                    region = region.trim();
                }
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
        // nothing to destroy; MinIO client holds no external resources that require closing
        client = null;
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
            throw new IOException("Unable to get presigned URL for " + can.container + "/" + can.name, e);
        }
    }

    @Override
    public InputStream getInputStream(String id, String root, String filePath) throws IOException {
        ContainerAndName can = getContainerAndName(id, root, filePath);
        try (InputStream responseStream = client.getObject(
                GetObjectArgs.builder().bucket(can.container).object(can.name).build())) {
            return materializeStream(responseStream);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Unable to read object " + can.container + "/" + can.name, e);
        }
    }

    @Override
    public long saveInputStream(String id, String root, String filePath, InputStream stream) throws IOException {
        if (stream == null) {
            return 0L;
        }

        ContainerAndName can = getContainerAndName(id, root, filePath);
        createContainerIfNotExist(can.container);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("id", Base64.encodeBase64String(id.getBytes(StandardCharsets.UTF_8)));
        metadata.put("path", filePath);

        try (CountingInputStream cis = new CountingInputStream(stream)) {
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(can.container)
                            .object(can.name)
                            .stream(cis, -1, partSize)
                            .userMetadata(metadata)
                            .build());
            return cis.getCount();
        } catch (Exception e) {
            throw new IOException("Unable to save object " + can.container + "/" + can.name, e);
        }
    }

    @Override
    public boolean delete(String id, String root, String filePath) {
        ContainerAndName can = getContainerAndName(id, root, filePath);
        try {
            client.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(can.container)
                            .object(can.name)
                            .build());
            return true;
        } catch (ErrorResponseException e) {
            ErrorResponse response = e.errorResponse();
            String code = response != null ? response.code() : null;
            if ("NoSuchKey".equals(code) || "NoSuchBucket".equals(code)) {
                return false;
            }
            log.error("Unable to delete object {}/{}", can.container, can.name, e);
            return false;
        } catch (Exception e) {
            log.error("Unable to delete object {}/{}", can.container, can.name, e);
            return false;
        }
    }

    @Override
    public long copy(String sourceId, String sourceRoot, String sourceFilePath,
                     String destId, String destRoot, String destFilePath) throws IOException {
        ContainerAndName src = getContainerAndName(sourceId, sourceRoot, sourceFilePath);
        ContainerAndName dst = getContainerAndName(destId, destRoot, destFilePath);
        createContainerIfNotExist(dst.container);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("id", Base64.encodeBase64String(destId.getBytes(StandardCharsets.UTF_8)));
        metadata.put("path", destFilePath);

        CopySource source = CopySource.builder()
                .bucket(src.container)
                .object(src.name)
                .build();

        try {
            client.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(dst.container)
                            .object(dst.name)
                            .source(source)
                            .metadataDirective(Directive.REPLACE)
                            .userMetadata(metadata)
                            .build());

            return client.statObject(
                    StatObjectArgs.builder()
                            .bucket(dst.container)
                            .object(dst.name)
                            .build()).size();
        } catch (Exception e) {
            throw new IOException("Unable to copy object " + src.container + "/" + src.name
                    + " to " + dst.container + "/" + dst.name, e);
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

        String path = useIdForPath ? id : filePath;
        String prefix = root == null ? "" : root;
        path = prefix + (path.startsWith("/") ? "" : "/") + path;
        path = path.replace("///", "/");
        path = path.replace("//", "/");
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (invalidCharactersRegex != null && !invalidCharactersRegex.isEmpty()) {
            path = path.replaceAll(invalidCharactersRegex, "");
        }

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

    private InputStream materializeStream(InputStream source) throws IOException {
        int initialCapacity = (int) Math.min(maxBlobStreamSize, 4L * 1024 * 1024);
        if (initialCapacity <= 0) {
            initialCapacity = 4096;
        }
        ByteArrayOutputStream memory = new ByteArrayOutputStream(initialCapacity);
        File tempFile = null;
        FileOutputStream fos = null;
        boolean usingFile = false;
        byte[] buffer = new byte[8192];
        int read;

        try {
            while ((read = source.read(buffer)) != -1) {
                if (!usingFile && (long) memory.size() + read > maxBlobStreamSize) {
                    usingFile = true;
                    tempFile = File.createTempFile("sakai-blob-", ".tmp");
                    fos = new FileOutputStream(tempFile);
                    memory.writeTo(fos);
                    memory.reset();
                }
                if (usingFile) {
                    fos.write(buffer, 0, read);
                } else {
                    memory.write(buffer, 0, read);
                }
            }
        } catch (IOException e) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException closeEx) {
                    e.addSuppressed(closeEx);
                }
            }
            deleteTempFile(tempFile);
            throw e;
        }

        if (usingFile) {
            try {
                fos.close();
            } catch (IOException closeEx) {
                deleteTempFile(tempFile);
                throw closeEx;
            }
            return inputStreamDeletingOnClose(tempFile);
        } else {
            return new ByteArrayInputStream(memory.toByteArray());
        }
    }

    private InputStream inputStreamDeletingOnClose(File file) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            final File fileToDelete = file;
            return new FilterInputStream(fis) {
                @Override
                public void close() throws IOException {
                    IOException ioException = null;
                    try {
                        super.close();
                    } catch (IOException e) {
                        ioException = e;
                    }
                    try {
                        if (fileToDelete.exists() && !fileToDelete.delete()) {
                            log.warn("Failed to delete temporary blob file {}", fileToDelete.getAbsolutePath());
                        }
                    } catch (SecurityException se) {
                        log.warn("Error deleting temporary blob file {}", fileToDelete.getAbsolutePath(), se);
                        if (ioException == null) {
                            ioException = new IOException("Error deleting temporary blob file " + fileToDelete.getAbsolutePath(), se);
                        } else {
                            ioException.addSuppressed(se);
                        }
                    }
                    if (ioException != null) {
                        throw ioException;
                    }
                }
            };
        } catch (IOException openEx) {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException closeEx) {
                    openEx.addSuppressed(closeEx);
                }
            }
            deleteTempFile(file);
            throw openEx;
        }
    }

    private void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        try {
            if (file.exists() && !file.delete()) {
                log.warn("Failed to delete temporary blob file {}", file.getAbsolutePath());
            }
        } catch (Exception e) {
            log.warn("Error deleting temporary blob file {}", file.getAbsolutePath(), e);
        }
    }

    /**
     * Simple counting {@link FilterInputStream}.
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

        @Override
        public void close() throws IOException {
            super.close();
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
