package uk.ac.cam.caret.sakai.rwiki.tool.bean;

import java.util.ArrayList;
import java.util.List;

public class AuthZGroupCollectionBean
{

	private ViewBean vb;

	private List currentRealms;

	public List getRealms()
	{
		if (currentRealms == null)
		{
			return new ArrayList();
		}
		return currentRealms;
	}

	public void setCurrentRealms(List currentRealms)
	{
		this.currentRealms = currentRealms;
	}

	public void setVb(ViewBean vb)
	{
		this.vb = vb;
	}

}
