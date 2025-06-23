package org.sakaiproject.test.e2e;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for E2E test setup and teardown operations.
 * Provides common functionality for Sakai end-to-end testing.
 */
public class TestUtils {
    
    private static final String TEST_PROPERTIES_FILE = "test.properties";
    private static Properties testProperties;
    
    static {
        loadTestProperties();
    }
    
    /**
     * Load test properties from configuration file
     */
    private static void loadTestProperties() {
        testProperties = new Properties();
        try (InputStream input = TestUtils.class.getClassLoader().getResourceAsStream(TEST_PROPERTIES_FILE)) {
            if (input != null) {
                testProperties.load(input);
            }
        } catch (IOException ex) {
            System.err.println("Failed to load test properties: " + ex.getMessage());
        }
        
        // Load system properties to override file properties
        testProperties.putAll(System.getProperties());
    }
    
    /**
     * Get base URL for Sakai instance
     */
    public static String getBaseUrl() {
        return getProperty("sakai.baseUrl", "http://localhost:8080");
    }
    
    /**
     * Get test username
     */
    public static String getTestUsername() {
        return getProperty("sakai.testUser", "admin");
    }
    
    /**
     * Get test password
     */
    public static String getTestPassword() {
        return getProperty("sakai.testPassword", "admin");
    }
    
    /**
     * Get property with default value
     */
    public static String getProperty(String key, String defaultValue) {
        return testProperties.getProperty(key, defaultValue);
    }
    
    /**
     * Get property value
     */
    public static String getProperty(String key) {
        return testProperties.getProperty(key);
    }
    
    /**
     * Check if running in CI environment
     */
    public static boolean isCI() {
        return "true".equals(System.getenv("CI")) || 
               "true".equals(System.getProperty("ci"));
    }
    
    /**
     * Get browser timeout in milliseconds
     */
    public static int getBrowserTimeout() {
        return Integer.parseInt(getProperty("sakai.browserTimeout", "30000"));
    }
    
    /**
     * Get page load timeout in milliseconds
     */
    public static int getPageLoadTimeout() {
        return Integer.parseInt(getProperty("sakai.pageLoadTimeout", "10000"));
    }
    
    /**
     * Setup test environment (called before test suite)
     */
    public static void setupTestEnvironment() {
        System.out.println("Setting up E2E test environment...");
        System.out.println("Base URL: " + getBaseUrl());
        System.out.println("Test User: " + getTestUsername());
        System.out.println("CI Mode: " + isCI());
    }
    
    /**
     * Cleanup test environment (called after test suite)
     */
    public static void cleanupTestEnvironment() {
        System.out.println("Cleaning up E2E test environment...");
        // Add any cleanup logic here
    }
    
    /**
     * Generate unique test data identifier
     */
    public static String generateTestId() {
        return "test_" + System.currentTimeMillis();
    }
    
    /**
     * Wait for a specified duration (in milliseconds)
     */
    public static void waitFor(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}