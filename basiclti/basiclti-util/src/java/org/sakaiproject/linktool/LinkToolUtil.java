package org.sakaiproject.linktool;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileInputStream;

import org.sakaiproject.component.cover.ServerConfigurationService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * Some Sakai Utility code for the Rutgers LinkTool.
 */
public class LinkToolUtil {

	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(LinkToolUtil.class);

	private static final String privkeyname = "sakai.rutgers.linktool.privkey";
	private static final String saltname = "sakai.rutgers.linktool.salt";

	private static Object sync_object = new Object();
	private static boolean LinkToolSetupComplete = false;
	private static String homedir = null;
	private static SecretKey secretKey = null;
	private static SecretKey salt = null;
	private static String ourUrl = null;

        private static SecretKey readSecretKey(String filename, String alg) {
            try {
                        FileInputStream file = new FileInputStream(filename);
                        byte[] bytes = new byte[file.available()];
                        file.read(bytes);
                        file.close();
                        SecretKey privkey = new SecretKeySpec(bytes, alg);
                        return privkey;
                } catch (Exception ignore) {
                        M_log.error("Unable to read key from " + filename);
                        return null;
            }
        }

	private static char[] hexChars = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
        };

        /**
         * Convert byte array to hex string
         * 
         * @param ba
         *        array of bytes
         * @throws Exception.
         */

        private static String byteArray2Hex(byte[] ba){
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < ba.length; i++){
                int hbits = (ba[i] & 0x000000f0) >> 4;
                int lbits = ba[i] & 0x0000000f;
                sb.append("" + hexChars[hbits] + hexChars[lbits]);
            }
            return sb.toString();
        }
        /**
         * Generate a secret key, and write it to a file
         * 
         * @param dirname
         *        writes to file privkeyname in this 
         *        directory. dirname assumed to end in /
         */

        private static void genkey(String dirname) 
	{
                try 
		{
                        /* Generate key. */
                        M_log.info("Generating new key in " + dirname + privkeyname);
                        SecretKey key = KeyGenerator.getInstance("Blowfish").generateKey();

                        /* Write private key to file. */
                        writeKey(key, dirname + privkeyname);
                } catch (Exception e) {
                        M_log.debug("Error generating key", e);
                }

        }

	/**
         * Writes <code>key</code> to file with name <code>filename</code>
         *
         * @throws IOException if something goes wrong.
         */
        private static void writeKey(Key key, String filename) 
	{
            try
            {
                        FileOutputStream file = new FileOutputStream(filename);
                        file.write(key.getEncoded());
                        file.close();
            }
            catch (FileNotFoundException e)
            {
                        M_log.error("Unable to write new key to " + filename);
            }
            catch (IOException e)
            {
                        M_log.error("Unable to write new key to " + filename);
            }
        }

        /**
         * Generate a random salt, and write it to a file
         * 
         * @param dirname
         *        writes to file saltname in this 
         *        directory. dirname assumed to end in /
         */

	private static void gensalt(String dirname) 
	{
            try {
                        // Generate a key for the HMAC-SHA1 keyed-hashing algorithm
                        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA1");
                        SecretKey key = keyGen.generateKey();
                        writeKey(key, dirname + saltname);
            } catch (Exception e) {
                M_log.warn("Error generating salt", e);
            }
        }

	public static String encrypt(String str) {
            LinkToolSetup();
            if ( secretKey == null ) return null;
            try {

                        Cipher ecipher = Cipher.getInstance("Blowfish");
                        ecipher.init(Cipher.ENCRYPT_MODE, secretKey);

                        // Encode the string into bytes using utf-8
                        byte[] utf8 = str.getBytes("UTF8");

                        // Encrypt
                        byte[] enc = ecipher.doFinal(utf8);

                        // Encode bytes to base64 to get a string
                        return byteArray2Hex(enc);
            } catch (javax.crypto.BadPaddingException e) {
                M_log.warn("linktool encrypt bad padding");
            } catch (javax.crypto.IllegalBlockSizeException e) {
                M_log.warn("linktool encrypt illegal block size");
            } catch (java.security.NoSuchAlgorithmException e) {
                M_log.warn("linktool encrypt no such algorithm");
            } catch (java.security.InvalidKeyException e) {
                M_log.warn("linktool encrypt invalid key");
            } catch (javax.crypto.NoSuchPaddingException e) {
                M_log.warn("linktool encrypt no such padding");
            } catch (java.io.UnsupportedEncodingException e) {
                M_log.warn("linktool encrypt unsupported encoding");
            }
            return null;
        }


    // Setup the LinkTool Environment Variables one time
    // If this fails, it does not re-try
    public static void LinkToolSetup()
    {
	if ( LinkToolSetupComplete ) return;

        // We will only do this once - so we make all wait here
        synchronized(sync_object) {
	    homedir = ServerConfigurationService.getString("linktool.home", ServerConfigurationService.getSakaiHomePath());
	    if (homedir == null) homedir = "/etc/";
	    if (!homedir.endsWith("/")) homedir = homedir + "/";

	    if (!(new File(homedir + privkeyname)).canRead()) {
		genkey(homedir);
	    }

	    secretKey = readSecretKey(homedir + privkeyname, "Blowfish");

	    if (!(new File(homedir + saltname)).canRead()) {
		gensalt(homedir);
	    }

	    salt = readSecretKey(homedir + saltname, "HmacSHA1");

            if ( salt != null && secretKey != null ) {
	        M_log.info("LinkToolSetup complete");
	    } else {
	        M_log.warn("LinkToolSetup failed - cannot create encrypted sessions");
	    }
            LinkToolSetupComplete = true;
	}
    }

}
