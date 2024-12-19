import static org.junit.Assert.*;
import org.junit.Test;
import org.tsugi.lti13.objects.LaunchJWT;
import org.tsugi.lti13.LTI13ConstantsUtil;

import java.util.Arrays;

public class LaunchJWTTest {

    @Test
    public void testDefaultConstructor() {
        LaunchJWT launchJWT = new LaunchJWT();
        assertNotNull(launchJWT);
        assertEquals(LaunchJWT.MESSAGE_TYPE_LAUNCH, launchJWT.message_type);
        assertEquals("1.3.0", launchJWT.version);
        assertNotNull(launchJWT.launch_presentation);
        assertNotNull(launchJWT.nonce);
    }

    @Test
    public void testParameterizedConstructor() {
        String customMessageType = LaunchJWT.MESSAGE_TYPE_DEEP_LINK;
        LaunchJWT customLaunchJWT = new LaunchJWT(customMessageType);
        assertNotNull(customLaunchJWT);
        assertEquals(customMessageType, customLaunchJWT.message_type);
        assertEquals("1.3.0", customLaunchJWT.version);
        assertNotNull(customLaunchJWT.launch_presentation);
        assertNotNull(customLaunchJWT.nonce);
    }

    @Test
    public void testGetDisplayName() {
        LaunchJWT launchJWT = new LaunchJWT();
        launchJWT.given_name = "John";
        launchJWT.middle_name = "A.";
        launchJWT.family_name = "Doe";
        assertEquals("John A. Doe", launchJWT.getDisplayName());

        launchJWT.name = "John Doe";
        assertEquals("John Doe", launchJWT.getDisplayName());

        launchJWT.name = null;
        launchJWT.given_name = null;
        launchJWT.middle_name = null;
        launchJWT.family_name = null;
        assertNull(launchJWT.getDisplayName());
    }

    @Test
    public void testIsInstructor() {
        LaunchJWT launchJWT = new LaunchJWT();
        launchJWT.roles = Arrays.asList(LTI13ConstantsUtil.ROLE_LEARNER, LTI13ConstantsUtil.ROLE_INSTRUCTOR);
        assertTrue(launchJWT.isInstructor());

        launchJWT.roles = Arrays.asList(LTI13ConstantsUtil.ROLE_LEARNER);
        assertFalse(launchJWT.isInstructor());

        launchJWT.roles = null;
        assertFalse(launchJWT.isInstructor());
    }

    @Test
    public void testGetLTI11Roles() {
        LaunchJWT launchJWT = new LaunchJWT();
        launchJWT.roles = Arrays.asList(LTI13ConstantsUtil.ROLE_LEARNER, LTI13ConstantsUtil.ROLE_INSTRUCTOR);
        assertEquals("http://purl.imsglobal.org/vocab/lis/v2/membership#Learner,http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor", launchJWT.getLTI11Roles());

        launchJWT.roles = null;
        assertNull(launchJWT.getLTI11Roles());

        launchJWT.roles = Arrays.asList();
        assertEquals("", launchJWT.getLTI11Roles());
    }
}

