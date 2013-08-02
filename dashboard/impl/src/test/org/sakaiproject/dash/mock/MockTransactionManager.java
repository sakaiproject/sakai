/**
 * 
 */
package org.sakaiproject.dash.mock;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

/**
 * @author jimeng
 *
 */
public class MockTransactionManager implements PlatformTransactionManager {

	/* (non-Javadoc)
	 * @see org.springframework.transaction.PlatformTransactionManager#getTransaction(org.springframework.transaction.TransactionDefinition)
	 */
	@Override
	public TransactionStatus getTransaction(TransactionDefinition definition)
			throws TransactionException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.transaction.PlatformTransactionManager#commit(org.springframework.transaction.TransactionStatus)
	 */
	@Override
	public void commit(TransactionStatus status) throws TransactionException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.springframework.transaction.PlatformTransactionManager#rollback(org.springframework.transaction.TransactionStatus)
	 */
	@Override
	public void rollback(TransactionStatus status) throws TransactionException {
		// TODO Auto-generated method stub

	}

}
