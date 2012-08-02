package org.sakaiproject.tool.assessment.ui.listener.delivery;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Set;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;


public class SaCharCountListener implements ActionListener
{
	private static Log log = LogFactory.getLog(SaCharCountListener.class);

	public void processAction(ActionEvent ae) throws AbortProcessingException
	{
		DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
		String itemId = ContextUtil.lookupParam("itemId");
		if (itemId == null) {
			return;
		}
		StringBuffer redrawAnchorName = new StringBuffer("p");

		Iterator iter = delivery.getPageContents().getPartsContents().iterator();
		while (iter.hasNext()) {
			SectionContentsBean part = (SectionContentsBean) iter.next();
			String partSeq = part.getNumber();
			redrawAnchorName.append(partSeq);
			Iterator iter2 = part.getItemContents().iterator();
			while (iter2.hasNext()) {
				ItemContentsBean item = (ItemContentsBean) iter2.next();
				ItemDataIfc itemData = item.getItemData();
				ArrayList itemGradingDataArray = item.getItemGradingDataArray();
				if (itemGradingDataArray != null && itemGradingDataArray.size() != 0) {
					Iterator iter3 = itemGradingDataArray.iterator();
					while (iter3.hasNext()) {
						ItemGradingData itemGrading = (ItemGradingData) iter3.next();
						if (itemData != null) {
							if (itemId.equals(itemData.getItemIdString()) && itemGrading.getAnswerText() != null) {
								if (itemGrading.getAnswerText() != null) {
									String processedAnswerText = itemGrading.getAnswerText().replaceAll("\r", "").replaceAll("\n", "");
									int saCharCount = processedAnswerText.length();
									String formattedCount = String.format("%,d\n",saCharCount);
									item.setSaCharCount(formattedCount);
									if (saCharCount > 60000) {
										item.setIsInvalidSALengthInput(true);
									}
									else {
										item.setIsInvalidSALengthInput(false);
									}
								}
								else {
									item.setSaCharCount("0");
									item.setIsInvalidSALengthInput(false);
								}
								redrawAnchorName.append("q");
								String itemSeq = itemData.getSequence().toString();
								redrawAnchorName.append(itemSeq);
								delivery.setRedrawAnchorName(redrawAnchorName.toString());
							}
						}
					}
				}
				else {
					if (itemId.equals(itemData.getItemIdString())) { 
						item.setSaCharCount("0");
						redrawAnchorName.append("q");
						String itemSeq = itemData.getSequence().toString();
						redrawAnchorName.append(itemSeq);
						delivery.setRedrawAnchorName(redrawAnchorName.toString());
					}
				}
			}
		}
		delivery.syncTimeElapsedWithServer();
	}
}
