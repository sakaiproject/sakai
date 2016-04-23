package coza.opencollab.sakai.cloudcontent;

/**
 * This interface allow for a logger for warnings and errors in 
 * the SwiftFileSystemHandler.
 * 
 * @author OpenCollab
 */
public interface CommonLogger {

    /**
     * Called if the space used on the account has reached a warning limit.
     * 
     * @param warningLimitInBytes The limit in bytes.
     * @param bytesUsed The amount of bytes used.
     */
    public void warningOnAccountSize(long warningLimitInBytes, long bytesUsed);

    /**
     * Called if the space used on the account has reached its limit.
     * 
     * @param maxSizeInBytes The max space available in bytes.
     * @param bytesUsed The amount of bytes used.
     */
    public void errorOnAccountSize(long maxSizeInBytes, long bytesUsed);

    /**
     * Called if the space used on the container has reached a warning limit.
     * 
     * @param warningLimitInBytes The limit in bytes.
     * @param bytesUsed The amount of bytes used.
     */
    public void warningOnContainerSize(long warningLimitInBytes, long bytesUsed);

    /**
     * Called if the space used on the container has reached its limit.
     * 
     * @param maxSizeInBytes The max space available in bytes.
     * @param bytesUsed The amount of bytes used.
     */
    public void errorOnContainerSize(long maxSizeInBytes, long bytesUsed);
    
}
