package org.sakaiproject.email.impl.test;

import org.sakaiproject.email.impl.BaseDigestService;

/** Just checks we don't need any missing methods as the main implementation is abstract.*/
public class ConcreteDigestService extends BaseDigestService {

	@Override
	protected Storage newStorage() {
		// TODO Auto-generated method stub
		return null;
	}

}
