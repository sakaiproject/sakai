package coza.opencollab.sakai.cloudcontent;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Closeables;
import com.google.common.io.CountingInputStream;
import com.google.inject.Module;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import lombok.extern.slf4j.Slf4j;

import org.jclouds.apis.ApiMetadata;
import org.jclouds.ContextBuilder;
import org.jclouds.http.options.GetOptions;
import org.jclouds.io.Payload;
import org.jclouds.io.Payloads;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.swift.v1.SwiftApi;
import org.jclouds.openstack.swift.v1.SwiftApiMetadata;
import org.jclouds.openstack.swift.v1.domain.SwiftObject;
import org.jclouds.openstack.swift.v1.features.ContainerApi;
import org.jclouds.openstack.swift.v1.features.ObjectApi;
import org.jclouds.openstack.swift.v1.options.CreateContainerOptions;
import org.jclouds.openstack.swift.v1.options.PutOptions;
import org.jclouds.osgi.ApiRegistry;

import org.springframework.util.FileCopyUtils;

import org.sakaiproject.content.api.FileSystemHandler;

/**
 * The cloud implementation of FileSystemHandler.
 * <p/>
 * This class read and write files to and from OpenStack-Swift cloud storage.
 *
 * @author OpenCollab
 */
@Slf4j
public class SwiftFileSystemHandler implements FileSystemHandler {

    /**
     * The provider for the jcloud storage.
     */
    private static final String CLOUD_PROVIDER = "openstack-swift";

    /**
     * The ApiMetadata class to register and use for Swift.
     */
    private static final ApiMetadata CLOUD_API_METADATA = new SwiftApiMetadata();
    /**
     * The connection endpoint to the swift storage.
     */
    private String endpoint;
    /**
     * The identity of the user to connect to swift.
     */
    private String identity;
    /**
     * The credential for the user.
     */
    private String credential;
    /**
     * The region to connect to.
     */
    private String region;
    /**
     * The swift storage api.
     */
    private SwiftApi swiftApi;
    /**
     * The base container to store everything under.
     * If this is not set then the filePath will specify the base container.
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
     * This is how long we want the signed URL to be valid for.
     */
    private static final int SIGNED_URL_VALIDITY_SECONDS = 10 * 60;

    /**
     * The logger for warnings and errors.
     */
    private CommonLogger logger = new DefaultLogger();
    /**
     * The limit where a error will be made using the CommonLogger.
     */
    private long errorLimitForAccountSizeInBytes = -1L;
    /**
     * The limit where a warning will be made using the CommonLogger.
     */
    private long warningLimitForAccountSizeInBytes = -1L;
    /**
     * The limit where a error will be made using the CommonLogger.
     */
    private long errorLimitForContainerSizeInBytes = -1L;
    /**
     * The limit where a warning will be made using the CommonLogger.
     */
    private long warningLimitForContainerSizeInBytes = -1L;
//    private boolean retrieveContainerFromIdWithRegex = false;
//    private String regexForContainerFromId = "";

    /**
     * Default constructor.
     */
    public SwiftFileSystemHandler() {
    }

    /**
     * The connection endpoint to the swift storage.
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * The identity of the user to connect to swift.
     */
    public void setIdentity(String identity) {
        this.identity = identity;
    }

    /**
     * The credential for the user.
     */
    public void setCredential(String credential) {
        this.credential = credential;
    }

    /**
     * The region to connect to.
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * The base container to store everything under.
     * If this is not set then the container will be retrieved from
     * the root and filePath/id.
     * 
     * Default is not set.
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
     * The limit where a error will be made using the CommonLogger.
     * If the value (default) is negative this will not be tested.
     * If positive then a exception will be thrown before a new resource is saved
     * and the bytes used in the account is already over this value.
     * A error will also be logged to the CommonLogger.
     * 
     * Note that this test is run before the save, thus it is possible that
     * the value can be exceeded after the save with no exception.
     */
    public void setErrorLimitForAccountSizeInBytes(long errorLimitForAccountSizeInBytes) {
        this.errorLimitForAccountSizeInBytes = errorLimitForAccountSizeInBytes;
    }

    /**
     * The limit where a warning will be made using the CommonLogger.
     * If the value (default) is negative this will not be tested.
     * If positive then a warning will be logged to the CommonLogger.
     * 
     * Note that this test is run before the save, thus it is possible that
     * the value can be exceeded after the save with no warning.
     */
    public void setWarningLimitForAccountSizeInBytes(long warningLimitForAccountSizeInBytes) {
        this.warningLimitForAccountSizeInBytes = warningLimitForAccountSizeInBytes;
    }

    /**
     * The limit where a error will be made using the CommonLogger.
     * If the value (default) is negative this will not be tested.
     * If positive then a exception will be thrown before a new resource is saved
     * and the bytes used in the container is already over this value.
     * A error will also be logged to the CommonLogger.
     * 
     * Note that this test is run before the save, thus it is possible that
     * the value can be exceeded after the save with no exception.
     */
    public void setErrorLimitForContainerSizeInBytes(long errorLimitForContainerSizeInBytes) {
        this.errorLimitForContainerSizeInBytes = errorLimitForContainerSizeInBytes;
    }

    /**
     * The limit where a warning will be made using the WarningLogger.
     * If the value (default) is negative this will not be tested.
     * If positive then a warning will be logged to the CommonLogger.
     * 
     * Note that this test is run before the save, thus it is possible that
     * the value can be exceeded after the save with no warning.
     */
    public void setWarningLimitForContainerSizeInBytes(long warningLimitForContainerSizeInBytes) {
        this.warningLimitForContainerSizeInBytes = warningLimitForContainerSizeInBytes;
    }

    /**
     * The logger for warnings and errors.
     */
    public void setLogger(CommonLogger logger) {
        this.logger = logger;
    }

    /**
     * Initiates the swift storage.
     */
    public void init() {
        Iterable<Module> modules = ImmutableSet.<Module>of(new SLF4JLoggingModule());

        // The metadata must be registered because the ServiceLoader does not detect
        // the META-INF/services in each api JAR under Tomcat/Spring. Fortunately,
        // the registry is static and is not really OSGi specific for this use.
        ApiRegistry.registerApi(CLOUD_API_METADATA);

        swiftApi = ContextBuilder.newBuilder(CLOUD_API_METADATA)
                .endpoint(endpoint)
                .credentials(identity, credential)
                .modules(modules)
                .buildApi(SwiftApi.class);
    }
    
    /**
     * Destroy/close the swift api.
     */
    public void destroy() throws IOException{
        Closeables.close(swiftApi, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getAssetDirectLink(String id, String root, String filePath) throws IOException {
        ContainerAndName can = getContainerAndName(id, root, filePath);
        ObjectApi objectApi = swiftApi.getObjectApi(region, can.container);
        SwiftObject so = objectApi.get(can.name, GetOptions.NONE);
        if(so == null){
            throw new IOException("No object found for " + id);
        }
        
        return so.getUri();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getInputStream(String id, String root, String filePath) throws IOException {
        ContainerAndName can = getContainerAndName(id, root, filePath);
        ObjectApi objectApi = swiftApi.getObjectApi(region, can.container);
        SwiftObject so = objectApi.get(can.name, GetOptions.NONE);
        if(so == null){
            throw new IOException("No object found for " + id);
        }
        //we copy this to a byte array first since Sakai does some funny stuff 
        //and with the stream and then swift and sakai don't play nice.
        return new ByteArrayInputStream(FileCopyUtils.copyToByteArray(so.getPayload().openStream()));
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
        checkAccountSpace();
        createContainerIfNotExist(can.container);
        checkContainerSpace(can.container);
        ObjectApi objectApi = swiftApi.getObjectApi(region, can.container);

        CountingInputStream in = null;
        Payload payload = null;

        try {
			in = new CountingInputStream((stream));
			payload = Payloads.newInputStreamPayload(in);
			objectApi.put(can.name, payload, PutOptions.Builder.metadata(ImmutableMap.of("id", id, "path", filePath)));
			return in.getCount();
		} finally {
			Closeables.close(stream, true);
			Closeables.close(in, true);
			payload.release();
		}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(String id, String root, String filePath) {
        ContainerAndName can = getContainerAndName(id, root, filePath);
        ObjectApi objectApi = swiftApi.getObjectApi(region, can.container);
        if(objectApi.getWithoutBody(can.name) == null){
            return false;
        }else{
            objectApi.delete(can.name);
            deleteContainerIfEmpty(can.container);
            return true;
        }
    }
    
    /**
     * Checks the space used for the account against the space available.
     * Will call the CommonLogger to report any warning or error.
     */
    private void checkAccountSpace() throws IOException{
        if(warningLimitForAccountSizeInBytes <= 0L && errorLimitForAccountSizeInBytes <= 0L){
            return;
        }
        long bytesUsed = swiftApi.getAccountApi(region).get().getBytesUsed();
        if(errorLimitForAccountSizeInBytes > 0L && errorLimitForAccountSizeInBytes < bytesUsed){
            logger.errorOnAccountSize(errorLimitForAccountSizeInBytes, bytesUsed);
            throw new IOException("No more space available for account!\nMax:" + errorLimitForAccountSizeInBytes + "\nUsed:" + bytesUsed);
        }
        //check warning after error since we don't want to raise a warning if error already raise.
        if(warningLimitForAccountSizeInBytes > 0L && warningLimitForAccountSizeInBytes < bytesUsed){
            logger.warningOnAccountSize(warningLimitForAccountSizeInBytes, bytesUsed);
        }
    }
    
    /**
     * Checks the space used for the container against the space available.
     * Will call the CommonLogger to report any warning or error.
     */
    private void checkContainerSpace(String container) throws IOException{
        if(warningLimitForContainerSizeInBytes <= 0L && errorLimitForContainerSizeInBytes <= 0L){
            return;
        }
        long bytesUsed = swiftApi.getContainerApi(region).get(container).getBytesUsed();
        if(errorLimitForContainerSizeInBytes > 0L && errorLimitForContainerSizeInBytes < bytesUsed){
            logger.errorOnContainerSize(errorLimitForContainerSizeInBytes, bytesUsed);
            throw new IOException("No more space available for container!\nMax:" + errorLimitForContainerSizeInBytes + "\nUsed:" + bytesUsed);
        }
        //check warning after error since we don't want to raise a warning if error already raise.
        if(warningLimitForContainerSizeInBytes > 0L && warningLimitForContainerSizeInBytes < bytesUsed){
            logger.warningOnContainerSize(warningLimitForContainerSizeInBytes, bytesUsed);
        }
    }

    /**
     * Make sure the container exist.
     */
    private void createContainerIfNotExist(String container) {
        ContainerApi containerApi = swiftApi.getContainerApi(region);
        CreateContainerOptions options = CreateContainerOptions.Builder.anybodyRead();
        containerApi.create(container, options);
    }
    
    /**
     * Delete the container if it is empty.
     */
    private void deleteContainerIfEmpty(String container){
        if(deleteEmptyContainers) {
            ContainerApi containerApi = swiftApi.getContainerApi(region);
            containerApi.deleteIfEmpty(container);
        }
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
    
    /**
     * A simple implementation that uses typical commons-logging.
     */
    class DefaultLogger implements CommonLogger {
        @Override
        public void warningOnAccountSize(long warningLimitInBytes, long bytesUsed) {
            log.warn("Warning on Swift account size -- warningLimit: {}, bytesUsed: {}", warningLimitInBytes, bytesUsed);
        }

        @Override
        public void errorOnAccountSize(long maxSizeInBytes, long bytesUsed) {
            log.error("Error on Swift account size -- maxSize: {}, bytesUsed: {}", maxSizeInBytes, bytesUsed);
        }

        @Override
        public void warningOnContainerSize(long warningLimitInBytes, long bytesUsed) {
            log.warn("Warning on Swift container size -- warningLimit: {}, bytesUsed: {}", warningLimitInBytes, bytesUsed);
        }

        @Override
        public void errorOnContainerSize(long maxSizeInBytes, long bytesUsed) {
            log.error("Error on Swift container size -- maxSize: {}, bytesUsed: {}", maxSizeInBytes, bytesUsed);
        }
    };
}
