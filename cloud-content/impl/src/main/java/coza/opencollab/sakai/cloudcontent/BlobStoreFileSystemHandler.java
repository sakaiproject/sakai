package coza.opencollab.sakai.cloudcontent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.FileSystemHandler;

import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
import lombok.Setter;

/**
 * MinIO-backed implementation of {@link FileSystemHandler} using the legacy
 * BlobStore configuration keys for drop-in compatibility with the former
 * jclouds handler.
 */
@Setter
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
    private String temporaryBlobDirectory;
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
        this.client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(identity, credential)
                .build();

        this.maxBlobStreamSize = serverConfigurationService.getInt("cloud.content.maxblobstream.size", 104857600);
        this.temporaryBlobDirectory = serverConfigurationService.getString("cloud.content.temporary.directory", null);
        int configuredPartSize = serverConfigurationService.getInt("cloud.content.multipart.partsize.mb", 10);
        this.partSize = Math.max(5, configuredPartSize) * 1024 * 1024;
        this.signedUrlExpiry = serverConfigurationService.getInt("cloud.content.signedurl.expiry", 600);
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
            StatObjectResponse stat = client.statObject(
                    StatObjectArgs.builder().bucket(can.container).object(can.name).build());
            long size = stat.size();
            InputStream in = client.getObject(
                    GetObjectArgs.builder().bucket(can.container).object(can.name).build());
            if (size > maxBlobStreamSize) {
                File tmp = (temporaryBlobDirectory != null)
                        ? File.createTempFile("minio", ".tmp", new File(temporaryBlobDirectory))
                        : File.createTempFile("minio", ".tmp");
                try (FileOutputStream fos = new FileOutputStream(tmp)) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        fos.write(buffer, 0, read);
                    }
                } finally {
                    in.close();
                }
                return new FileInputStream(tmp);
            } else {
                return in;
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
            headers.put("x-amz-meta-id", Base64.encodeBase64String(id.getBytes("UTF-8")));
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
            return false;
        }
    }

    private void createContainerIfNotExist(String container) throws Exception {
        boolean exists = client.bucketExists(b -> b.bucket(container));
        if (!exists) {
            client.makeBucket(b -> b.bucket(container));
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

