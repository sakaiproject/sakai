package org.sakaiproject.content.tool;

import org.sakaiproject.util.ResourceLoader;
import java.util.ArrayList;
/**
 *
 * class encapsulates information about groups of metadata tags (such as DC, LOM, etc.)
 *
 */
	public class MetadataGroup extends ArrayList
	{
		/**
		 *
		 */
		private static final long serialVersionUID = -821054142728929236L;
		protected boolean m_isShowing;
		protected String m_name;
		protected ResourceLoader metaMsg = new ResourceLoader("types");
		protected ResourceLoader rb = new ResourceLoader("content");

		/**
		 * @param name
		 */
		public MetadataGroup(String name)
		{
			super();
			m_name = name;
			m_isShowing = false;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 * needed to determine List.contains()
		 */
		public boolean equals(Object obj)
		{
			if (obj instanceof MetadataGroup) {
				MetadataGroup mg = (MetadataGroup) obj;
				boolean rv = (obj != null) && (m_name.equals(mg.getName()));
				return rv;
			}
			return false;
		}

		/**
		 * @return
		 */
		public String getName(){
			String name = rb.getString(m_name);
			if (name.indexOf("missing_key")!=-1)
				return m_name;
			else
				return name;
		}


		/**
		 * @return
		 */
		public boolean isShowing()
		{
			return m_isShowing;
		}

		/**
		 * @param name
		 */
		public void setName(String name)
		{
			m_name = name;
		}

		/**
		 * @param isShowing
		 */
		public void setShowing(boolean isShowing)
		{
			m_isShowing = isShowing;
		}
		
		public String getShowLabel()
		{
			return metaMsg.getFormattedMessage("metadata.show", new String[]{this.getName()});
		}
		
		public String getHideLabel()
		{
			return metaMsg.getFormattedMessage("metadata.hide", new String[]{this.getName()});
		}

	}
