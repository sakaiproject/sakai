package org.sakaiproject.tool.assessment.ui.listener.author;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ValueChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class FavoriteColChoiceListener implements ValueChangeListener {
	 private static Log log = LogFactory.getLog(StartCreateItemListener.class);
	public void processValueChange(ValueChangeEvent ae) throws AbortProcessingException
	  {
	    log.debug("FavoriteColChoiceListener: valueChangeLISTENER.");
	    ItemAuthorBean itemauthorbean = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");


	    String selectedvalue= (String) ae.getNewValue();
	    
	    if ((selectedvalue!=null) && (!selectedvalue.equals("")) ){
	    	ItemBean curritem = itemauthorbean.getCurrentItem();
	    	if(curritem != null){
	    		curritem.setCurrentFavorite(selectedvalue);
	    		curritem.setColumnChoicesFromFavorite(selectedvalue);
	    		curritem.setFavoriteName(selectedvalue);
	    	}

	    }
	  }


}
