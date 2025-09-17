package org.tsugi.lti13;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.tsugi.lti13.objects.LaunchJWT;

import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public class LaunchJWTTest {
    
    private LaunchJWT launchJWT;
    
    @Before
    public void setUp() {
        launchJWT = new LaunchJWT();
    }

    @Test
    public void testDefaultConstructor() {
        // Test default values
        assertNotNull("LaunchJWT should not be null", launchJWT);
        assertEquals("Message type should be LtiResourceLinkRequest", 
            LaunchJWT.MESSAGE_TYPE_LAUNCH, launchJWT.message_type);
        assertEquals("Version should be 1.3.0", "1.3.0", launchJWT.version);
        assertNotNull("Launch presentation should not be null", launchJWT.launch_presentation);
        assertNotNull("Nonce should not be null", launchJWT.nonce);
        
        // Test initial state of other fields
        assertNull("Subject should initially be null", launchJWT.subject);
        assertNull("Issuer should initially be null", launchJWT.issuer);
        assertNull("Audience should initially be null", launchJWT.audience);
        assertNull("Deployment ID should initially be null", launchJWT.deployment_id);
        assertNotNull("Roles should initially be an empty list", launchJWT.roles);
        assertTrue("Roles should initially be empty", launchJWT.roles.isEmpty());
    }

    @Test
    public void testParameterizedConstructor() {
        // Test with deep linking message type
        String customMessageType = LaunchJWT.MESSAGE_TYPE_DEEP_LINK;
        LaunchJWT customLaunchJWT = new LaunchJWT(customMessageType);
        assertEquals("Message type should match constructor parameter", 
            customMessageType, customLaunchJWT.message_type);
        assertEquals("Version should be 1.3.0", "1.3.0", customLaunchJWT.version);
        assertNotNull("Launch presentation should not be null", customLaunchJWT.launch_presentation);
        assertNotNull("Nonce should not be null", customLaunchJWT.nonce);
        
        // Test with null message type (should use default)
        customLaunchJWT = new LaunchJWT(null);
        assertEquals("Null message type should default to LtiResourceLinkRequest",
            LaunchJWT.MESSAGE_TYPE_LAUNCH, customLaunchJWT.message_type);
    }

    @Test
    public void testGetDisplayName() {
        // Test full name with all components
        launchJWT.given_name = "John";
        launchJWT.middle_name = "A.";
        launchJWT.family_name = "Doe";
        assertEquals("Full name should combine all components correctly", 
            "John A. Doe", launchJWT.getDisplayName());

        // Test name field takes precedence
        launchJWT.name = "John Doe";
        assertEquals("Name field should take precedence", 
            "John Doe", launchJWT.getDisplayName());

        // Test partial names
        launchJWT.name = null;
        launchJWT.middle_name = null;
        assertEquals("Should handle missing middle name", 
            "John Doe", launchJWT.getDisplayName());

        launchJWT.family_name = null;
        assertEquals("Should handle only given name", 
            "John", launchJWT.getDisplayName());

        // Test empty case
        launchJWT.given_name = null;
        assertNull("Should return null when no name components present", 
            launchJWT.getDisplayName());
    }

    @Test
    public void testIsInstructor() {
        // Test instructor role
        launchJWT.roles = Arrays.asList(LTI13ConstantsUtil.ROLE_INSTRUCTOR);
        assertTrue("Should identify instructor role", launchJWT.isInstructor());

        // Test multiple roles including instructor
        launchJWT.roles = Arrays.asList(
            LTI13ConstantsUtil.ROLE_LEARNER,
            LTI13ConstantsUtil.ROLE_INSTRUCTOR,
            LTI13ConstantsUtil.ROLE_CONTEXT_ADMIN
        );
        assertTrue("Should identify instructor among multiple roles", launchJWT.isInstructor());

        // Test non-instructor role
        launchJWT.roles = Arrays.asList(LTI13ConstantsUtil.ROLE_LEARNER);
        assertFalse("Should not identify learner as instructor", launchJWT.isInstructor());

        // Test empty roles list
        launchJWT.roles = Collections.emptyList();
        assertFalse("Should not identify empty roles as instructor", launchJWT.isInstructor());

        // Test null roles
        launchJWT.roles = null;
        assertFalse("Should handle null roles gracefully", launchJWT.isInstructor());
    }

    @Test
    public void testGetLTI11Roles() {
        // Test multiple roles
        List<String> roles = Arrays.asList(
            LTI13ConstantsUtil.ROLE_LEARNER,
            LTI13ConstantsUtil.ROLE_INSTRUCTOR
        );
        launchJWT.roles = roles;
        assertEquals("Should correctly join multiple roles",
            "http://purl.imsglobal.org/vocab/lis/v2/membership#Learner," +
            "http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor",
            launchJWT.getLTI11Roles());

        // Test single role
        launchJWT.roles = Collections.singletonList(LTI13ConstantsUtil.ROLE_LEARNER);
        assertEquals("Should handle single role correctly",
            "http://purl.imsglobal.org/vocab/lis/v2/membership#Learner",
            launchJWT.getLTI11Roles());

        // Test empty roles list
        launchJWT.roles = new ArrayList<>();
        assertEquals("Should return empty string for empty roles list",
            "", launchJWT.getLTI11Roles());

        // Test null roles
        launchJWT.roles = null;
        assertNull("Should return null for null roles", launchJWT.getLTI11Roles());
    }
}

