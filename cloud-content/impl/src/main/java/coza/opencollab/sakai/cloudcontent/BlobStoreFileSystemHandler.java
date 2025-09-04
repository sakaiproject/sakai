package coza.opencollab.sakai.cloudcontent;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.jclouds.ContextBuilder;
import org.jclouds.aws.s3.AWSS3ProviderMetadata;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.MultipartPart;
import org.jclouds.blobstore.domain.MultipartUpload;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.options.PutOptions;
import org.jclouds.http.HttpRequest;
import org.jclouds.io.Payload;
import org.jclouds.io.Payloads;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.swift.v1.SwiftApiMetadata;
import org.jclouds.osgi.ApiRegistry;
import org.jclouds.osgi.ProviderRegistry;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.FileSystemHandler;
import org.springframework.util.FileCopyUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Closeables;
import com.google.inject.Module;

import lombok.Setter;

/**
 * The jclouds BlobStore implementation of FileSystemHandler.
 * <p/>
 * This class read and write files to and from a BlobStore provider.
 *
 * @author OpenCollab
 * @author botimer
 */
@Setter
public class BlobStoreFileSystemHandler implements FileSystemHandler {

    /**
     * The BlobStore context (connection).
     */
    private BlobStoreContext context;

    /**
     * ServerConfigurationService injected via components.xml
     */
    private ServerConfigurationService serverConfigurationService;

    /**
     * The jclouds BlobStore provider name to use.
     *
     * The provider must be packaged with the component and registered with
     * the ServiceLoader or ProviderRegistry. By default, these are only
     * aws-s3 and openstack-swift.
     */
    private String provider = "aws-s3";

    /**
     * The connection endpoint to the s3-compatible storage.
     */
    private String endpoint = "https://s3.amazonaws.com";

    /**
     * The identity/user to connect to the BlobStore.
     */
    private String identity;

    /**
     * The credential for the identity/user.
     */
    private String credential;

    /**
     * The base container/bucket to use.
     *
     * Default is not set, but it is required for e.g., S3.
     */
    private String baseContainer;

    /**
     * Whether to delete empty containers after a resource delete and there
     * is no more resources in the container.
     *
     * The Default is false.
     */
    private boolean deleteEmptyContainers = false;

    /**
     * Whether to use the id for the resource path.
     *
     * The default is false, so the filePath will be used.
     */
    private boolean useIdForPath = false;

    /**
     * The regular expression for all the characters that is not valid 
     * for container and resource names.
     * Default is null.
     */
    private String invalidCharactersRegex = "[:*?<|>]";

    /**
     * This is how long we want the signed URL to be valid for.
     */
    private static final int SIGNED_URL_VALIDITY_SECONDS = 10 * 60;

    /**
     * The largest a blob can be before we write it to temp file to avoid OOMing Tomcat
     */
    private static long maxBlobStreamSize = 1024 * 1024 * 100;
    
    /** 
     * A preferred directory to write temporary blobs. Maybe a large, temporary partition?
     */
    private static String temporaryBlobDirectory;

    /**
     * Default constructor.
     */
    public BlobStoreFileSystemHandler() {
    }

    /**
     * Initializes the BlobStore context.
     */
    public void init() {
        Iterable<Module> modules = ImmutableSet.<Module>of(new SLF4JLoggingModule());

        // The metadata must be registered because the ServiceLoader does not detect
        // the META-INF/services in each api JAR under Tomcat/Spring. Fortunately,
        // the registry is static and is not really OSGi specific for this use.
        ApiRegistry.registerApi(new SwiftApiMetadata());
        ProviderRegistry.registerProvider(new AWSS3ProviderMetadata());

        context = ContextBuilder.newBuilder(provider)
                .endpoint(endpoint)
                .credentials(identity, credential)
                .modules(modules)
                .buildView(BlobStoreContext.class);

        // There are some oddities with streaming larger files to the user,
        // so download to a temp file first. For now, call 100MB the threshold.
        maxBlobStreamSize = (long) serverConfigurationService.getInt("cloud.content.maxblobstream.size", 1024 * 1024 * 100);
        temporaryBlobDirectory = serverConfigurationService.getString("cloud.content.temporary.directory", null);
        
        if (temporaryBlobDirectory != null) {
            File baseDir = new File(temporaryBlobDirectory);
            if (!baseDir.exists()) {
                try {
                    // Can't write into the preferred temp dir
                    if (!baseDir.mkdirs()) {
                        temporaryBlobDirectory = null;
                    }
                }
                catch (SecurityException se) {
                    // JVM security hasn't whitelisted this dir
                    temporaryBlobDirectory = null;
                }
            }
        }
    }
    
    /**
     * Destroy/close the BlobStore context/connection.
     */
    public void destroy() throws IOException{
        Closeables.close(context, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getAssetDirectLink(String id, String root, String filePath) throws IOException {
        ContainerAndName can = getContainerAndName(id, root, filePath);
        HttpRequest hr = context.getSigner().signGetBlob(can.container, can.name, SIGNED_URL_VALIDITY_SECONDS);
        if (hr == null) {
            throw new IOException("No object found to creat signed url " + id);
        }

        return hr.getEndpoint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getInputStream(String id, String root, String filePath) throws IOException {
        ContainerAndName can = getContainerAndName(id, root, filePath);
        Blob blob = getBlobStore().getBlob(can.container, can.name);
        if (blob == null){
            throw new IOException("No object found for " + id);
        }

        StorageMetadata metadata = blob.getMetadata();
        Long size = metadata.getSize();

        if (size != null && size > maxBlobStreamSize) {
            return streamFromTempFile(blob, size);
        } else {
            // SAK-30325: why can't we just send the stream straight back: blob.getPayload().openStream() ?
            // Good question, but it doesn't work properly unless the stream is fully copied and re-streamed....
            return new ByteArrayInputStream(FileCopyUtils.copyToByteArray(blob.getPayload().openStream()));
        }
    }

    // Hacky implementation of downloading Blobs to temp files...
    // This should probably happen in a specified location and use
    // hashing to be sure of contents before reusing.
    private InputStream streamFromTempFile(Blob blob, Long filesize) {
        StorageMetadata metadata = blob.getMetadata();
        String name = metadata.getName();
        String filename = name + "-" + filesize;
        filename = DigestUtils.md5Hex(filename);
        FileInputStream stream = null;

        // See if the temp file already exists
        File check;
        if (temporaryBlobDirectory != null) {
            check = new File(temporaryBlobDirectory, filename);
        }
        else {
            check = new File(System.getProperty("java.io.tmpdir"), filename);
        }

        if (check.exists()) {
            try {
                stream = new FileInputStream(check);
            } catch (FileNotFoundException e) {
                stream = null;
            }
        } else {
            try {
                FileOutputStream fos = new FileOutputStream(check);
                FileCopyUtils.copy(blob.getPayload().openStream(), fos);
                stream = new FileInputStream(check);
            } catch (IOException e) {
                stream = null;
            }
        }

        return stream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
        public long saveInputStream(String id, String root, String filePath, InputStream stream) throws IOException {
            if(stream == null){
                return 0L;
            }
            ContainerAndName can = getContainerAndName(id, root, filePath);
            createContainerIfNotExist(can.container);

            // Use multipart upload for streaming without knowing content length upfront
            BlobStore store = getBlobStore();
            String asciiID = Base64.encodeBase64String(id.getBytes(StandardCharsets.UTF_8));

            // Use multipart upload to avoid having to know the content length
            // This allows streaming directly without buffering or double-reading
            store.blobBuilder(can.name)
                .userMetadata(ImmutableMap.of("id", asciiID, "path", filePath))
                .build();

            // Initiate multipart upload
            MultipartUpload mpu = store.initiateMultipartUpload(
                can.container,
                store.blobBuilder(can.name)
                    .userMetadata(ImmutableMap.of("id", asciiID, "path", filePath))
                    .build().getMetadata(),
                PutOptions.NONE
            );

            List<MultipartPart> parts = new ArrayList<>();
            long totalSize = 0;
            int partNumber = 1;

            // Stream chunks of 5MB minimum (S3 requirement)
            final int CHUNK_SIZE = 5 * 1024 * 1024; // 5MB chunks
            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;

            try {
                while ((bytesRead = readFully(stream, buffer)) > 0) {
                    // Create a payload for this part
                    Payload partPayload = Payloads.newByteArrayPayload(Arrays.copyOf(buffer, bytesRead));

                    // Upload this part
                    MultipartPart part = store.uploadMultipartPart(
                        mpu,
                        partNumber,
                        partPayload
                    );
                    parts.add(part);
                    totalSize += bytesRead;
                    partNumber++;
                    
                    partPayload.release();
                }

                // Complete the multipart upload
                store.completeMultipartUpload(mpu, parts);

            } catch (Exception e) {
                // Abort the multipart upload on error
                store.abortMultipartUpload(mpu);
                throw new IOException("Multipart upload failed", e);
            } finally {
                Closeables.close(stream, true);
            }

            return totalSize;
        }

        /**
         * Helper method to read fully into buffer
         */
        private int readFully(InputStream stream, byte[] buffer) throws IOException {
            int totalRead = 0;
            while (totalRead < buffer.length) {
                int read = stream.read(buffer, totalRead, buffer.length - totalRead);
                if (read == -1) {
                    break;
                }
                totalRead += read;
            }
            return totalRead;
        }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(String id, String root, String filePath) {
        ContainerAndName can = getContainerAndName(id, root, filePath);

        BlobStore store = getBlobStore();
        if (!store.blobExists(can.container, can.name)) {
            return false;
        } else {
            store.removeBlob(can.container, can.name);
            deleteContainerIfEmpty(can.container);
            return true;
        }
    }
    
    /**
     * Make sure the container exists.
     */
    private void createContainerIfNotExist(String container) {
        getBlobStore().createContainerInLocation(null, container);
    }
    
    /**
     * Delete the container if it is empty.
     */
    private void deleteContainerIfEmpty(String container){
        if(deleteEmptyContainers) {
            getBlobStore().deleteContainerIfEmpty(container);
        }
    }

    private BlobStore getBlobStore() {
        return context.getBlobStore();
    }
    
    /**
     * This method set the container and file name.
     * It will first validate the parameters and ensure that they 
     * contain valid characters.
     * If the parameters aren't valid this method will throw 
     * a IllegalArgumentException.
     */
    private ContainerAndName getContainerAndName(String id, String root, String filePath) throws IllegalArgumentException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("The id cannot be null or empty!");
        }
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("The path cannot be null or empty!");
        }
        String path = (useIdForPath?id:filePath);
        path = (root==null?"":root) + (path.startsWith("/")?"":"/") + path;
        //fix double slash and starting slash
        //The multi slash is a major problem for swift, so making sure her we get it out.
        path = path.replace("///", "/");
        path = path.replace("//", "/");
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        //fix invalid chars
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
     * Just a containing class for the container name and file path and name.
     */
    class ContainerAndName {
        String container;
        String name;
    }

}
