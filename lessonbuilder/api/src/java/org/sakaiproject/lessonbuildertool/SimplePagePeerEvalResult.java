
package org.sakaiproject.lessonbuildertool;

import java.util.Date;

public interface SimplePagePeerEvalResult {
	/*	<id name="peerEvalResultlId" column="PEER_EVAL_RESULT_ID" type="long">
					<generator class="native">
						<param name="sequence">LB_PEER_EVAL_S</param>
					</generator>
		</id>
		
		<property name="pageId" type="long">
			<column name="PAGE_ID" not-null="true"/>
		</property>	
		<property name="UUID" type="string" length="36" not-null="true" />
		<property name="timePosted" type="java.util.Date" >
			<column name="TIME_POSTED" not-null="false"/>
		</property>
		<property name="grader" type="java.util.String" >
			<column name="" length="255" not-null="true"/>
		</property>
		<property name="gradee" type="java.util.String" >
			<column name="" length="255" not-null="true"/>
		</property>
		<property name="rowId" type="long" >
            	<column name="ROW_ID" not-null="true"/>
     	</property>
     	<property name="columnValue" type="integer">
     		<column name="COLUMN_VALUE" not-null="true"/>
     	</property>
     	<property name="selected" type="boolean">
     		<column name="SELECTED" not-null="false"/>
     	</property>
     	*/
	
	public long getPeerEvalResultId();
	public void setPeerEvalResultId(long id);
	
	public long getPageId();
	public void setPageId(long id);
	
	public Date getTimePosted();
	public void setTimePosted(Date date);
	
	public String getGrader();
	public void setGrader(String author);
	
	public String getGradee();
	public void setGradee(String author);

	public String getRowText();
	public void setRowText(String text);
	
	public int getColumnValue();
	public void setColumnValue(int value);

	public boolean getSelected();
	public void setSelected(boolean selected);
	
	public int compareTo(Object o);

}
