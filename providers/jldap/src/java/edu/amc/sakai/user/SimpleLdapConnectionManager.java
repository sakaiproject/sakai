package edu.amc.sakai.user;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPConstraints;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPTLSSocketFactory;

/**
 * Allocates connected, constrained, and optionally
 * bound and secure <code>LDAPConnections</code>
 * 
 * @see LdapConnectionManagerConfig
 * @author Dan McCallum, Unicon Inc
 * @author John Lewis, Unicon Inc
 */
public class SimpleLdapConnectionManager implements LdapConnectionManager {
	
	public static final String KEYSTORE_LOCATION_SYS_PROP_KEY = 
		"javax.net.ssl.trustStore";
	public static final String KEYSTORE_PASSWORD_SYS_PROP_KEY = 
		"javax.net.ssl.trustStorePassword";
	
	/** Class-specific logger */
	private static Log M_log = LogFactory.getLog(SimpleLdapConnectionManager.class);
	
	/** connection allocation configuration */
	private LdapConnectionManagerConfig config;
	
	/**
	 * {@inheritDoc}
	 */
	public void init() {
		
		if ( M_log.isDebugEnabled() ) {
			M_log.debug("init()");
		}
		
		if ( config.isSecureConnection() ) {
			if ( M_log.isDebugEnabled() ) {
				M_log.debug("init(): initializing secure socket factory");
			}
			initKeystoreLocation();
			initKeystorePassword();
			LDAPConnection.setSocketFactory(config.getSecureSocketFactory());
		}
			
	}
	
	/**
	 * {@inheritDoc}
	 */
	public LDAPConnection getConnection() throws LDAPException {
		
		if ( M_log.isDebugEnabled() ) {
			M_log.debug("getConnection()");
		}
		
		LDAPConnection conn = new LDAPConnection();
		applyConstraints(conn);
		connect(conn);

		if ( config.isAutoBind() ) {
			if ( M_log.isDebugEnabled() ) {
				M_log.debug("getConnection(): auto-binding");
			}
			try {
				bind(conn, config.getLdapUser(), config.getLdapPassword());
			} catch (LDAPException ldape) {
				if (ldape.getResultCode() == LDAPException.INVALID_CREDENTIALS) {
					M_log.warn("Failed to bind against: "+ conn.getHost()+ " with user: "+ config.getLdapUser()+ " password: "+ config.getLdapPassword().replaceAll(".", "*"));
				}
				throw ldape;
			}
		}

		return conn;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public LDAPConnection getBoundConnection(String dn, String pw) throws LDAPException {
		
		if ( M_log.isDebugEnabled() ) {
			M_log.debug("getBoundConnection(): [dn = " + dn + "]");
		}
		
		LDAPConnection conn = new LDAPConnection();
		applyConstraints(conn);
		connect(conn);
		bind(conn, dn, pw);

		return conn;
	}

	private void bind(LDAPConnection conn, String dn, String pw) 
	throws LDAPException {
		
		if ( M_log.isDebugEnabled() ) {
			M_log.debug("bind(): binding [dn = " + dn + "]");
		}
		
		try {
			conn.bind(LDAPConnection.LDAP_V3, dn, pw.getBytes("UTF8"));
		} catch ( UnsupportedEncodingException e ) {
			throw new RuntimeException("Failed to encode user password", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void returnConnection(LDAPConnection conn) {
		try {
			if (conn != null)
				conn.disconnect();
		} catch (LDAPException e) {
			M_log.error("returnConnection(): failed on disconnect: ", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setConfig(LdapConnectionManagerConfig config) {
		this.config = config;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public LdapConnectionManagerConfig getConfig() {
		return config;
	}
	
	
	/**
	 * Caches a keystore password as a system property. No-op
	 * if the {@link #KEYSTORE_PASSWORD_SYS_PROP_KEY} system property
	 * has a non-<code>null</code> value. Otherwise caches the
	 * keystore password from the currently assigned 
	 * {@link LdapConnectionManagerConfig}.
	 * 
	 * @param config
	 * @throws NullPointerException if a non-null keystore password
	 *   cannot be resolved
	 */
	protected void initKeystorePassword() {
		if ( M_log.isDebugEnabled() ) {
			M_log.debug("initKeystorePassword()");
		}
		String sysKeystorePassword = 
			System.getProperty(KEYSTORE_PASSWORD_SYS_PROP_KEY);
		if ( sysKeystorePassword == null ) {
			String configuredKeystorePassword = config.getKeystorePassword();
			if ( configuredKeystorePassword != null){
				if ( M_log.isDebugEnabled() ) {
					M_log.debug("initKeystorePassword(): setting system property");
				}
				System.setProperty(KEYSTORE_PASSWORD_SYS_PROP_KEY, configuredKeystorePassword);
			}
		} else {
			if ( M_log.isDebugEnabled() ) {
				M_log.debug("initKeystorePassword(): retained existing system property");
			}
		}
	}
	
	
	/**
	 * Caches a keystore location as a system property. No-op
	 * if the {@link #KEYSTORE_LOCATION_SYS_PROP_KEY} system property
	 * has a non-<code>null</code> value. Otherwise caches the
	 * keystore location from the currently assigned 
	 * {@link LdapConnectionManagerConfig}.
	 * 
	 * @param config
	 * @throws NullPointerException if a non-null keystore location
	 *   cannot be resolved
	 */
	protected void initKeystoreLocation() {
		if ( M_log.isDebugEnabled() ) {
			M_log.debug("initKeystoreLocation()");
		}
		String sysKeystoreLocation = 
			System.getProperty(KEYSTORE_LOCATION_SYS_PROP_KEY);
		if ( sysKeystoreLocation == null ) {
			String configuredKeystoreLocation = config.getKeystoreLocation();
			if ( configuredKeystoreLocation != null){
				if ( M_log.isDebugEnabled() ) {
					M_log.debug("initKeystoreLocation(): setting system property [location = " + 
							configuredKeystoreLocation + "]");
				}
				System.setProperty(KEYSTORE_LOCATION_SYS_PROP_KEY, configuredKeystoreLocation);
			}
		} else {
			if ( M_log.isDebugEnabled() ) {
				M_log.debug("initKeystoreLocation(): retained existing system property [location = " + 
						sysKeystoreLocation + "]");
			}
		}
	}
	
	/**
	 * Applies <code>LDAPConstraints</code>
	 * to the specified <code>LDAPConnection</code>.
	 * Implemented to assign <code>timeLimit</code> and 
	 * <code>referralFollowing</code> constraint values 
	 * retrieved from the currently assigned
	 * {@link LdapConnectionManagerConfig}.
	 * 
	 * @param conn
	 */
	protected void applyConstraints(LDAPConnection conn) {
		int timeout = config.getOperationTimeout();
		boolean followReferrals = config.isFollowReferrals();
		if ( M_log.isDebugEnabled() ) {
			M_log.debug("applyConstraints(): values [timeout = " + 
					timeout + "][follow referrals = " + followReferrals + "]");
		}
		LDAPConstraints constraints = new LDAPConstraints();
		constraints.setTimeLimit(timeout);
		constraints.setReferralFollowing(followReferrals);
		conn.setConstraints(constraints);
	}
	
	/**
	 * Connects the specified <code>LDAPConnection</code> to
	 * the currently configured host and port.
	 * 
	 * @param conn an <code>LDAPConnection</code>
	 * @throws LDAPConnection if the connect attempt fails
	 */
	protected void connect(LDAPConnection conn) throws LDAPException {
		if ( M_log.isDebugEnabled() ) {
			M_log.debug("connect()");
		}
		
		conn.connect(config.getLdapHost(), config.getLdapPort());
		
		try {
			postConnect(conn);
		} catch ( LDAPException e ) {
			M_log.error("Failed to completely initialize a connection [host = " + 
					config.getLdapHost() + "][port = " + 
					config.getLdapPort() + "]", e);
			try {
				conn.disconnect();
			} catch ( LDAPException ee ) {}
			
			throw e;
		} catch ( Throwable e ) {
			M_log.error("Failed to completely initialize a connection [host = " + 
					config.getLdapHost() + "][port = " + 
					config.getLdapPort() + "]", e);
			try {
				conn.disconnect();
			} catch ( LDAPException ee ) {}
			
			if ( e instanceof Error ) {
				throw (Error)e;
			}
			if ( e instanceof RuntimeException ) {
				throw (RuntimeException)e;
			}
			
			throw new RuntimeException("LDAPConnection allocation failure", e);
		}
		
	}
	
	protected void postConnect(LDAPConnection conn) throws LDAPException {
		
		if ( M_log.isDebugEnabled() ) {
			M_log.debug("postConnect()");
		}
		
		if ( config.isSecureConnection() && isTlsSocketFactory() ) {
			if ( M_log.isDebugEnabled() ) {
				M_log.debug("postConnect(): starting TLS");
			}
			conn.startTLS();
		}
	}

	protected boolean isTlsSocketFactory() {
		return config.getSecureSocketFactory() instanceof LDAPTLSSocketFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	public void destroy() {
	}

}
