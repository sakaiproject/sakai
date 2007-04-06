package org.adl.datamodels;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.adl.datamodels.ieee.SCORM_2004_DM;
import org.adl.datamodels.ieee.SCORM_2004_DMElement;

public class SCORM_2004_DMElementTestCase extends HibernateTestCase {
	
	public void testPersistence() throws Exception {
		SCORM_2004_DM dataModel = new SCORM_2004_DM();
		
		//DMElement element = dataModel.getDMElement("comments_from_learner");
		
		put(dataModel);
		
		//System.out.println("ELEM: " + element.mDescription.mBinding);
		
		List<SCORM_2004_DM> list = find(SCORM_2004_DM.class);
		
		//find(DMDelimiter.class);
		
		//List<DMDelimiter> list = (List<DMDelimiter>)find(DMDelimiter.class);
		
		//System.out.println("List size is " + list.size());
		
		//SCORM_2004_DM elem = dataModel;
		
		for (SCORM_2004_DM elem : list) {
			
			System.out.println("Binding " + elem.getDMBindingString());
			
			elem.showAllElements();
		}
		
		//DMElementDescriptor iDescription,
        //DMElement iParent,
        //SCORM_2004_DM iDM
	}

	
}
