package coza.opencollab.sakai.cloudcontent;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Closeables;
import com.google.inject.Module;

import org.jclouds.ContextBuilder;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.options.PutOptions;
import org.jclouds.io.Payload;
import org.jclouds.io.Payloads;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;

import org.jclouds.aws.s3.AWSS3ProviderMetadata;
import org.jclouds.openstack.swift.v1.SwiftApiMetadata;
import org.jclouds.osgi.ApiRegistry;
import org.jclouds.osgi.ProviderRegistry;

import org.sakaiproject.content.api.FileSystemHandler;
import org.springframework.util.FileCopyUtils;

/**
 * The jclouds BlobStore implementation of FileSystemHandler.
 * <p/>
 * This class read and write files to and from a BlobStore provider.
 *
 * @author OpenCollab
 * @author botimer
 */
public class BlobStoreFileSystemHandler implements FileSystemHandler {

    /**
     * The BlobStore context (connection).
     */
    private BlobStoreContext context;
    /**
     * The jclouds provider name to use.
     */
    private String provider = "aws-s3";
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
     */
    private String baseContainer;
    /**
     * Whether to delete empty containers after a resource delete.
     */
    private boolean deleteEmptyContainers = false;
    /**
     * Whether to use the id for the file path.
     */
    private boolean useIdForPath = false;
    /**
     * The regular expression for all the characters that is not valid 
     * for container and resource names.
     */
    private String invalidCharactersRegex = "[:*?<|>]";
    /**
     * The maximum buffer size, which dictates the maximum file upload.
     *
     * Because services like S3 require a known size at the beginning of an
     * upload, we buffer the InputStream to get its size. This is not
     * the default size of the buffer, but the maximum. The
     * content.upload.ceiling property is almost certainly lower than the 1GB
     * set here, meaning that the buffer should be bounded on it instead.
     */
    private static final int MAX_UPLOAD_BYTES = 1024 * 1024 * 1024;

    /**
     * Default constructor.
     */
    public BlobStoreFileSystemHandler() {
    }

    /**
     * The jclouds BlobStore provider name to use.
     *
     * The provider must be packaged with the component and registered with
     * the ServiceLoader or ProviderRegistry. By default, these are only
     * aws-s3 and openstack-swift.
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * The identity/user to connect to the BlobStore.
     */
    public void setIdentity(String identity) {
        this.identity = identity;
    }

    /**
     * The credential for the identity/user.
     */
    public void setCredential(String credential) {
        this.credential = credential;
    }

    /**
     * The base container/bucket to use.
     *
     * Default is not set, but it is required for e.g., S3.
     */
    public void setBaseContainer(String baseContainer) {
        this.baseContainer = baseContainer;
    }

    /**
     * Whether to delete empty containers after a resource delete and there 
     * is no more resources in the container.
     * 
     * The Default is false.
     */
    public void setDeleteEmptyContainers(boolean deleteEmptyContainers) {
        this.deleteEmptyContainers = deleteEmptyContainers;
    }
    
    /**
     * Whether to use the id for the resource path.
     * 
     * The default is false, so the filePath will be used.
     */
    public void setUseIdForPath(boolean useIdForPath){
        this.useIdForPath = useIdForPath;
    }

    /**
     * The regular expression for all the characters that is not valid 
     * for container and resource names.
     * Default is null.
     */
    public void setInvalidCharactersRegex(String invalidCharactersRegex) {
        this.invalidCharactersRegex = invalidCharactersRegex;
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
                .credentials(identity, credential)
                .modules(modules)
                .buildView(BlobStoreContext.class);
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
    public InputStream getInputStream(String id, String root, String filePath) throws IOException {
        ContainerAndName can = getContainerAndName(id, root, filePath);
        Blob blob = getBlobStore().getBlob(can.container, can.name);
        if (blob == null){
            throw new IOException("No object found for " + id);
        }
        //we copy this to a byte array first since Sakai does some funny stuff 
        //and with the stream and then swift and sakai don't play nice.
        return new ByteArrayInputStream(FileCopyUtils.copyToByteArray(blob.getPayload().openStream()));
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


        InputStream in = markableInputStream(stream);
        long size = markableStreamLength(in);

        Payload payload = Payloads.newInputStreamPayload(in);
        BlobStore store = getBlobStore();
        Blob blob = store.blobBuilder(can.name)
            .payload(payload)
            .contentLength(size)
            .userMetadata(ImmutableMap.of("id", id, "path", filePath))
            .build();
        store.putBlob(can.container, blob);

        return size;
    }

    /**
     * Get a markable version of an InputStream, wrapping it if necessary.
     *
     * This method will return the passed stream if it is already markable,
     * otherwise wrapping it in a BufferedInputStream to support mark/reset
     * for computing length and rereading.
     */
    private InputStream markableInputStream(InputStream stream) {
        if (stream.markSupported()) {
            return stream;
        } else {
            return new BufferedInputStream(stream);
        }
    }

    /**
     * Get the length of a markable InputStream.
     */
    private long markableStreamLength(InputStream stream) throws IOException {
        long size = 0;
        stream.mark(MAX_UPLOAD_BYTES);
        while (stream.read() != -1) {
            size += 1;
        }
        stream.reset();
        return size;
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
        if (id == null || id.trim().length() == 0) {
            throw new IllegalArgumentException("The id cannot be null or empty!");
        }
        if (filePath == null || filePath.trim().length() == 0) {
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
