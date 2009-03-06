package org.sakaiproject.sitestats.test.mocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Tool;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FakeSitePage implements SitePage {
	private String siteId;
	private String toolId;
	
	public FakeSitePage(String siteId, String toolId) {
		this.siteId = siteId;
		this.toolId = toolId;
	}
	
	public ToolConfiguration addTool() {
		// TODO Auto-generated method stub
		return null;
	}

	public ToolConfiguration addTool(Tool arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ToolConfiguration addTool(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Site getContainingSite() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getLayout() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getLayoutTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getSiteId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSkin() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	public ToolConfiguration getTool(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public List getTools() {
		List<ToolConfiguration> tc = new ArrayList<ToolConfiguration>();
		tc.add(new FakeToolConfiguration(toolId));
		return tc;
	}

	public List getTools(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection getTools(String[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isPopUp() {
		// TODO Auto-generated method stub
		return false;
	}

	public void moveDown() {
		// TODO Auto-generated method stub

	}

	public void moveUp() {
		// TODO Auto-generated method stub

	}

	public void removeTool(ToolConfiguration arg0) {
		// TODO Auto-generated method stub

	}

	public void setLayout(int arg0) {
		// TODO Auto-generated method stub

	}

	public void setPopup(boolean arg0) {
		// TODO Auto-generated method stub

	}

	public void setPosition(int arg0) {
		// TODO Auto-generated method stub

	}

	public void setTitle(String arg0) {
		// TODO Auto-generated method stub

	}

	public void setupPageCategory(String arg0) {
		// TODO Auto-generated method stub

	}

	public ResourcePropertiesEdit getPropertiesEdit() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isActiveEdit() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	public ResourceProperties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getReference() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getReference(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUrl(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Element toXml(Document arg0, Stack arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
