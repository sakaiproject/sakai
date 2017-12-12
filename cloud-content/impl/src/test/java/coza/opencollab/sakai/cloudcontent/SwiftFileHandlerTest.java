package coza.opencollab.sakai.cloudcontent;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.springframework.util.FileCopyUtils;

/**
 *
 * @author OpenCollab
 */
@Slf4j
public class SwiftFileHandlerTest {
    private static final SwiftFileSystemHandler swift = new SwiftFileSystemHandler();
    private static final String BASE_CONTAINER = "unit-tests";
    private static final String MESSAGE = "Hello World";
    private static final byte[] BINARY = new byte[136548];
    private static final String ID0 = "";
    private static final String ID1 = "id";
    private static final String ID2 = "id/group/filename";
    private static final String ID3 = "id/group/something/985/147/longer/x24r42ff-c4-c334f-4c-y34x34l";
    private static final String ID4 = "id/group/binary/filename";
    private static final String ROOT0 = "";
    private static final String ROOT1 = "root";
    private static final String PATH0 = "";
    private static final String PATH1 = "path";
    private static final String PATH_VALID = "path/more/filename";
    private static final String PATH2 = PATH_VALID;
    private static final String PATH3 = "path/something/234/543/long/d24r42ff-c4-c334f-4c-c34c34c";
    private static final String PATH4 = "path/more/binary/filename";
    private static final String PATH_INVALID = "path>/m<o:re/fi|le*name?";
    private static final String PATH_WITH_DOUBLE_SLASH = "path//more//filename";
    
    public SwiftFileHandlerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        Properties props = new Properties();
        try {
            props.load(SwiftFileHandlerTest.class.getResourceAsStream("/swift.properties"));
        } catch(IOException e) {
            log.error("Cannot read Swift configuration (src/test/resources/swift.properties)... Bailing out.");
            throw e;
        }

        swift.setEndpoint(props.getProperty("endpoint"));
        swift.setRegion(props.getProperty("region"));
        swift.setIdentity(props.getProperty("identity"));
        swift.setCredential(props.getProperty("credential"));
        swift.setDeleteEmptyContainers(Boolean.valueOf(props.getProperty("deleteEmptyContainers")));

        swift.init();

        Random r = new Random(System.currentTimeMillis());
        r.nextBytes(BINARY);
    }
    
    @AfterClass
    public static void tearDownClass() throws IOException {
        swift.destroy();
    }
    
    private InputStream getInputStream(){
        return new ByteArrayInputStream(MESSAGE.getBytes());
    }
    
    private InputStream getBinaryInputStream(){
        return new ByteArrayInputStream(BINARY);
    }

    @Test
    public void testSaveNullStream() throws IOException, IOException, IOException {
        assertEquals(0L, swift.saveInputStream(null, null, null, null));
        assertEquals(0L, swift.saveInputStream(ID1, ROOT1, PATH1, null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveNullId() throws IOException, IOException, IOException {
        swift.saveInputStream(null, ROOT1, PATH1, getInputStream());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveNullPath() throws IOException, IOException, IOException {
        swift.saveInputStream(ID1, ROOT1, null, getInputStream());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveEmptyId() throws IOException {
        swift.saveInputStream(ID0, ROOT1, PATH1, getInputStream());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveEmptyPath() throws IOException {
        swift.saveInputStream(ID1, ROOT1, PATH0, getInputStream());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNoContainer1() throws IOException {
        swift.setBaseContainer(null);
        swift.setUseIdForPath(false);
        swift.saveInputStream(ID1, ROOT0, PATH1, getInputStream());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoContainer2() throws IOException {
        swift.setBaseContainer(null);
        swift.setUseIdForPath(false);
        swift.saveInputStream(ID1, null, PATH1, getInputStream());
    }
    
    private SwiftFileSystemHandler.ContainerAndName getContainerAndName(String id, String root, String filePath) throws Exception{
        Method m = SwiftFileSystemHandler.class.getDeclaredMethod("getContainerAndName", String.class, String.class, String.class);
        m.setAccessible(true);
        return (SwiftFileSystemHandler.ContainerAndName) m.invoke(swift, id, root, filePath);
    }
    
    @Test
    public void testUseIdOrPath() throws Exception{
        swift.setUseIdForPath(true);
        swift.setBaseContainer(BASE_CONTAINER);
        SwiftFileSystemHandler.ContainerAndName can = getContainerAndName(ID2, null, PATH2);
        assertEquals(BASE_CONTAINER, can.container);
        assertEquals(ID2, can.name);
        swift.setUseIdForPath(false);
        can = getContainerAndName(ID2, null, PATH2);
        assertEquals(PATH2, can.name);
    }
    
    @Test
    public void testSlash() throws Exception{
        swift.setBaseContainer(BASE_CONTAINER);
        SwiftFileSystemHandler.ContainerAndName can = getContainerAndName(ID2, "/" + ROOT1 + "//", PATH_WITH_DOUBLE_SLASH);
        assertFalse(can.name.startsWith("/"));
        assertFalse(can.name.contains("//"));
    }
    
    @Test
    public void testInvalidChars() throws Exception{
        swift.setBaseContainer(BASE_CONTAINER);
        swift.setInvalidCharactersRegex("[:*?<|>]");
        swift.setUseIdForPath(false);
        SwiftFileSystemHandler.ContainerAndName can = getContainerAndName(ID2, ROOT0, PATH_INVALID);
        assertEquals(PATH_VALID, can.name);
    }
    
    @Test
    public void testValid() throws IOException {
        long contentSize = swift.saveInputStream(ID2, ROOT1, PATH2, getInputStream());
        log.debug("{}:{}", contentSize, FileCopyUtils.copy(getInputStream(), new ByteArrayOutputStream()));
        assertEquals(11L, contentSize);
        String message = FileCopyUtils.copyToString(new InputStreamReader(swift.getInputStream(ID2, ROOT1, PATH2)));
        assertEquals(MESSAGE, message);
        swift.delete(ID2, ROOT1, PATH2);
        try{
            FileCopyUtils.copyToString(new InputStreamReader(swift.getInputStream(ID2, ROOT1, PATH2)));
            assertNull("Should not get here");
        }catch(IOException e){
            assertNotNull(e);
        }
    }
    
    @Test
    public void testValidText() throws IOException {
        long contentSize = swift.saveInputStream(ID3, ROOT1, PATH3, getInputStream());
        log.debug("{}", contentSize);
        assertEquals(11L, contentSize);
        String message = FileCopyUtils.copyToString(new InputStreamReader(swift.getInputStream(ID3, ROOT1, PATH3)));
        assertEquals(MESSAGE, message);
        swift.delete(ID3, ROOT1, PATH3);
    }
    
    @Test
    public void testValidBinary() throws IOException {
        long contentSize = swift.saveInputStream(ID4, ROOT1, PATH4, getBinaryInputStream());
        log.debug("Binary size: {}({})", contentSize, FileCopyUtils.copy(getBinaryInputStream(), new ByteArrayOutputStream()));
        assertEquals(136548L, contentSize);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertEquals(contentSize, FileCopyUtils.copy(swift.getInputStream(ID4, ROOT1, PATH4), out));
        assertArrayEquals(BINARY, out.toByteArray());
        swift.delete(ID4, ROOT1, PATH4);
    }
}
