package edu.amc.sakai.user;

import java.io.UnsupportedEncodingException;

import com.novell.ldap.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Allocates connected, constrained, and optionally
 * bound and secure <code>LDAPConnections</code>
 * 
 * @see LdapConnectionManagerConfig
 * @author Dan McCallum, Unicon Inc
 * @author John Lewis, Unicon Inc
 */
@Slf4j
public class SimpleLdapConnectionManager implements LdapConnectionManager {
	
	public static final String KEYSTORE_LOCATION_SYS_PROP_KEY = 
		"javax.net.ssl.trustStore";
	public static final String KEYSTORE_PASSWORD_SYS_PROP_KEY = 
		"javax.net.ssl.trustStorePassword";

	/** connection allocation configuration */
	private LdapConnectionManagerConfig config;
	
	/**
	 * {@inheritDoc}
	 */
	public void init() {
		
		if ( log.isDebugEnabled() ) {
			log.debug("init()");
		}
		
		if ( config.isSecureConnection() ) {
			if ( log.isDebugEnabled() ) {
				log.debug("init(): initializing keystore");
			}
			initKeystoreLocation();
			initKeystorePassword();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public LDAPConnection getConnection() throws LDAPException {
		
		if ( log.isDebugEnabled() ) {
			log.debug("getConnection()");
		}
		LDAPConnection conn = newConnection();

		if ( config.isAutoBind() ) {
			if ( log.isDebugEnabled() ) {
				log.debug("getConnection(): auto-binding");
			}
			try {
				bind(conn, config.getLdapUser(), config.getLdapPassword());
			} catch (LDAPException ldape) {
				if (ldape.getResultCode() == LDAPException.INVALID_CREDENTIALS) {
					log.warn("Failed to bind against: "+ conn.getHost()+ " with user: "+ config.getLdapUser()+ " password: "+ config.getLdapPassword().replaceAll(".", "*"));
				}
				throw ldape;
			}
		}

		return conn;
	}

	/**
	 * Return a new LDAPConnection with the appropriate socket factory set for the connection type.
	 */
	private LDAPConnection createConnectionWithSocketFactory() {
		LDAPSocketFactory factory;

		if (config.isSecureConnection()) {
			factory = config.getSecureSocketFactory();

			if (factory == null) {
				throw new RuntimeException("You must set a 'secureSocketFactory' (in jldap-beans.xml) when using LDAPS");
			}
		} else {
			factory = config.getSocketFactory();
		}

		if (factory == null) {
			return new LDAPConnection();
		} else {
			return new LDAPConnection(factory);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public LDAPConnection getBoundConnection(String dn, String pw) throws LDAPException {
		
		if ( log.isDebugEnabled() ) {
			log.debug("getBoundConnection(): [dn = " + dn + "]");
		}
		
		LDAPConnection conn = createConnectionWithSocketFactory();
		applyConstraints(conn);
		connect(conn);
		bind(conn, dn, pw);

		return conn;
	}

	protected LDAPConnection newConnection() throws LDAPException {

		if (log.isDebugEnabled() ) {
			log.debug("newConnection()");
		}
		LDAPConnection connection = createConnectionWithSocketFactory();
		applyConstraints(connection);
		connect(connection);
		return connection;
	}

	private void bind(LDAPConnection conn, String dn, String pw)
	throws LDAPException {
		
		if ( log.isDebugEnabled() ) {
			log.debug("bind(): binding [dn = " + dn + "]");
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
			log.error("returnConnection(): failed on disconnect: ", e);
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
		if ( log.isDebugEnabled() ) {
			log.debug("initKeystorePassword()");
		}
		String sysKeystorePassword = 
			System.getProperty(KEYSTORE_PASSWORD_SYS_PROP_KEY);
		if ( sysKeystorePassword == null ) {
			String configuredKeystorePassword = config.getKeystorePassword();
			if ( configuredKeystorePassword != null){
				if ( log.isDebugEnabled() ) {
					log.debug("initKeystorePassword(): setting system property");
				}
				System.setProperty(KEYSTORE_PASSWORD_SYS_PROP_KEY, configuredKeystorePassword);
			}
		} else {
			if ( log.isDebugEnabled() ) {
				log.debug("initKeystorePassword(): retained existing system property");
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
		if ( log.isDebugEnabled() ) {
			log.debug("initKeystoreLocation()");
		}
		String sysKeystoreLocation = 
			System.getProperty(KEYSTORE_LOCATION_SYS_PROP_KEY);
		if ( sysKeystoreLocation == null ) {
			String configuredKeystoreLocation = config.getKeystoreLocation();
			if ( configuredKeystoreLocation != null){
				if ( log.isDebugEnabled() ) {
					log.debug("initKeystoreLocation(): setting system property [location = " + 
							configuredKeystoreLocation + "]");
				}
				System.setProperty(KEYSTORE_LOCATION_SYS_PROP_KEY, configuredKeystoreLocation);
			}
		} else {
			if ( log.isDebugEnabled() ) {
				log.debug("initKeystoreLocation(): retained existing system property [location = " + 
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
		if ( log.isDebugEnabled() ) {
			log.debug("applyConstraints(): values [timeout = " + 
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
		if ( log.isDebugEnabled() ) {
			log.debug("connect()");
		}
		
		conn.connect(config.getLdapHost(), config.getLdapPort());
		
		try {
			postConnect(conn);
		} catch ( LDAPException e ) {
			log.error("Failed to completely initialize a connection [host = " + 
					config.getLdapHost() + "][port = " + 
					config.getLdapPort() + "]", e);
			try {
				conn.disconnect();
			} catch ( LDAPException ee ) {}
			
			throw e;
		} catch ( Throwable e ) {
			log.error("Failed to completely initialize a connection [host = " + 
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
		
		if ( log.isDebugEnabled() ) {
			log.debug("postConnect()");
		}
		
		if ( config.isSecureConnection() && isTlsSocketFactory() ) {
			if ( log.isDebugEnabled() ) {
				log.debug("postConnect(): starting TLS");
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
