/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package edu.amc.sakai.user;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * This class exists to demonstrate lifecycle method firing from
 * {@link GenericObjectPool}. It was specififcally motivated by
 * problems with stale {@link PooledLDAPConnection} objects
 * being returned to the pool.
 * 
 * <p>The fixture is intended to mimic the pool created by
 * {@link PoolingLdapConnectionManager#init()} and new tests
 * should probably be created if that behavior/configuration
 * changes.</p>
 * 
 * <p>Again, keep in mind that this class exists purely
 * for demonstation purposes, i.e. to learn about the pooling
 * library, not to verify LDAP provider behaviors. Also keep
 * in mind that pool behaviors, including lifecycle method
 * invocation, are highly dependent on configuration, so
 * the behaviors verified in this class may not hold for
 * a different fixture configuration</p>
 * 
 * @author Dan McCallum (dmccallum@unicon.net)
 *
 */
public class GenericObjectPoolTest extends MockObjectTestCase {

    private GenericObjectPool pool;
    private PoolableObjectFactory factory;
    private Mock mockFactory;
    
    protected void setUp() throws Exception {

        mockFactory = new Mock(PoolableObjectFactory.class);
        factory = (PoolableObjectFactory)mockFactory.proxy();
        
        pool = new GenericObjectPool(factory,
                1, // maxActive
                GenericObjectPool.WHEN_EXHAUSTED_BLOCK, // whenExhaustedAction
                60000, // maxWait (millis)
                1, // maxIdle
                true, // testOnBorrow
                false // testOnReturn
                );
        
        super.setUp();
    }
    
    /**
     * Verifies that {@link GenericObjectPool#borrowObject()} fires 
     * {@link PoolableObjectFactory#makeObject()}, 
     * {@link PoolableObjectFactory#activateObject(Object)},
     * and {@link PoolableObjectFactory#validateObject(Object)}, in that order.
     * 
     * @throws Exception test error
     */
    public void testBorrowObjectFiresMakeActivateAndValidate() throws Exception {
        
        Object pooledObject = new Object();
        
        // expectations are implemented as a stack, so are searched in the reverse
        // order from which they were created
        mockFactory.expects(once()).method("validateObject").with(same(pooledObject)).will(returnValue(true));
        mockFactory.expects(once()).method("activateObject").with(same(pooledObject));
        mockFactory.expects(once()).method("makeObject").will(returnValue(pooledObject));
        
        Object borrowedObject = pool.borrowObject();
        assertSame("Unexpected object returned from pool", pooledObject, borrowedObject);
        
    }
    
    /**
     * Verifies that {@link GenericObjectPool} makes no subsequent calls
     * to {@link PoolableObjectFactory#makeObject()} following a failed
     * {@link PoolableObjectFactory#validateObject(Object)} <em>on a newly
     * created poolable object</em>. This was unexpected behavior --
     * initially this was a test case that verified the pool falling back
     * to alloc a second new object when validation on a new object failed.
     * It makes sense -- without a retry limit, the pool could fall into
     * an endless alloc-validate loop.
     * 
     * <p>Note that this test also verifies that the pool fires 
     * {@link PoolableObjectFactory#destroyObject(Object)} when validation
     * fails</p>
     *
     * @throws Exception test error
     */
    public void testWillNotRetryMakeObjectOnFailedValidate() throws Exception {
        
        Object pooledObject1 = new Object();
        
        mockFactory.expects(once()).method("makeObject").
            will(returnValue(pooledObject1));
        mockFactory.expects(once()).method("activateObject").
    	    with(same(pooledObject1)).after("makeObject");
        mockFactory.expects(once()).method("validateObject").
            with(same(pooledObject1)).after("activateObject").will(returnValue(false));
        mockFactory.expects(once()).method("destroyObject").
            with(same(pooledObject1)).after("validateObject");
        
        try {
            pool.borrowObject();
            fail("Should failed to borrow object");
        } catch ( Exception e ) {
            // success
        }
        
        // rely on JMock to validate mockFactory
          
    }
    
    /**
     * Similar to {@link #testWillNotRetryMakeObjectOnFailedValidate()} but
     * verifies that the pool attempts new object creation when validation
     * fails on an object <em>already present in the pool</em>.
     *
     * @throws Exception test error
     */
    public void testAttemptsToPoolNewObjectOnFailedValidationOfPooledObject() 
    throws Exception {
        
        Object pooledObject1 = new Object();
        Object pooledObject2 = new Object();
 
        // expectations are implemented as a stack, so are searched in the reverse
        // order from which they were create
        
        mockFactory.expects(once()).method("validateObject").
            with(same(pooledObject2)).will(returnValue(true));
        mockFactory.expects(once()).method("activateObject").with(same(pooledObject2));
        // alloc a second object
        mockFactory.expects(once()).method("makeObject").
            will(returnValue(pooledObject2));
        // make sure the original object is destroyed
        mockFactory.expects(once()).method("destroyObject").with(same(pooledObject1));
        // a second borrow should reactivate and reverify the original object -- we'll
        // fail the validation to force alloc of a new poolable object
        mockFactory.expects(once()).method("validateObject").
            with(same(pooledObject1)).will(returnValue(false));
        mockFactory.expects(once()).method("activateObject").with(same(pooledObject1));
        // client adds the object back to the pool
        mockFactory.expects(once()).method("passivateObject").with(same(pooledObject1));
        // pass the validation, pooledObject1 should be in the pool
        mockFactory.expects(once()).method("validateObject").
            with(same(pooledObject1)).will(returnValue(true));
        mockFactory.expects(once()).method("activateObject").with(same(pooledObject1));
        mockFactory.expects(once()).method("makeObject").
            will(returnValue(pooledObject1));
        
        Object borrowedObject1 = pool.borrowObject();
        // something of a sanity check
        assertSame("Unexpected object returned from pool", pooledObject1, borrowedObject1);
        pool.returnObject(borrowedObject1);
        
        Object borrowedObject2 = pool.borrowObject();
        // now make sure we got a branch new object
        assertSame("Unexpected object returned from pool", pooledObject2, borrowedObject2);
        
    }
    
    /**
     * Verifies a suspicion that if a null reference is returned to the pool, the pool
     * will return that reference to subsequent {@link GenericObjectPool#borrowObject()}
     * calls, so long as the object passes activation and validation lifecycle phases.
     * 
     * @throws Exception
     */
    public void testPoolAllowsNullObjectReferencesToBeReturnedAndSubsequentlyBorrowed() throws Exception {
    	
        mockFactory.expects(once()).method("passivateObject").with(NULL);
        mockFactory.expects(once()).method("activateObject").with(NULL).after("passivateObject");
        
        // this is really the important expectation -- the underlying factory must be
        // implemented such that it fails to identify "null" objects as invalid
        mockFactory.expects(once()).method("validateObject").with(NULL).after("activateObject").will(returnValue(true));
        
        
        pool.returnObject(null); // the code exercise
        assertNull(pool.borrowObject());
    	
    }
    
}
