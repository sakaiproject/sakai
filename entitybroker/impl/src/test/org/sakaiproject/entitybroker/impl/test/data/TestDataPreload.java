/**
 * TestDataPreload.java - created by Sakai App Builder -AZ
 */

package org.sakaiproject.entitybroker.impl.test.data;

import org.sakaiproject.entitybroker.dao.model.EntityProperty;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.genericdao.api.GenericDao;

/**
 * Contains test data for preloading and test constants
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class TestDataPreload {

    public TestData td = new TestData();

    public GenericDao dao;
    public void setDao(GenericDao dao) {
        this.dao = dao;
    }

    public void init() {
        preloadTestData(dao);
    }


    // testing data objects here
    public EntityProperty prop1 = new EntityProperty( TestData.REF1, TestData.PREFIX1, TestData.PROPERTY_NAME1, TestData.PROPERTY_VALUE1 );
    public EntityProperty prop1B = new EntityProperty( TestData.REF1, TestData.PREFIX1, TestData.PROPERTY_NAME1B, TestData.PROPERTY_VALUE1B );
    public EntityProperty prop1C = new EntityProperty( TestData.REF1, TestData.PREFIX1, TestData.PROPERTY_NAME1C, TestData.PROPERTY_VALUE1C );

    public EntityProperty prop1_1 = new EntityProperty( TestData.REF1_1, TestData.PREFIX1, TestData.PROPERTY_NAME1_1, TestData.PROPERTY_VALUE1_1 );

    public EntityProperty prop2 = new EntityProperty( TestData.REF2, TestData.PREFIX2, TestData.PROPERTY_NAME2, TestData.PROPERTY_VALUE2 );

    public EntityProperty prop3 = new EntityProperty( TestData.REF3, TestData.PREFIX3, TestData.PROPERTY_NAME3, TestData.PROPERTY_VALUE3 );
    public EntityProperty prop3B = new EntityProperty( TestData.REF3, TestData.PREFIX3, TestData.PROPERTY_NAME3B, TestData.PROPERTY_VALUE3B );


    /**
     * Preload a bunch of test data into the database
     * @param dao a generic dao
     */
    public boolean preloaded = false;
    public void preloadTestData(GenericDao dao) {
        dao.save(prop1);
        dao.save(prop1B);
        dao.save(prop1C);

        dao.save(prop1_1);

        dao.save(prop2);

        dao.save(prop3);
        dao.save(prop3B);

        preloaded = true;
    }


}
