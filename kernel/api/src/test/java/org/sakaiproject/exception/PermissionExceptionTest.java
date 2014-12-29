package org.sakaiproject.exception;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PermissionExceptionTest {

	/**
	 * Check that we get a sensible message from a permission exception.
	 */
	@Test
	public void checkExceptionMessage() {
		PermissionException pe = new PermissionException("userId", "a.lock.that.failed", "/some/sakai/reference");
		assertNotNull(pe.getMessage());
		assertNotSame("", pe.getMessage());
		
		assertNotNull(pe.toString());
		assertNotSame("", pe.toString());
	}

}
