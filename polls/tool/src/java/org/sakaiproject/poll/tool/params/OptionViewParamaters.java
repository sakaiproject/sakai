package org.sakaiproject.poll.tool.params;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class OptionViewParamaters extends SimpleViewParameters {

		public String id;
		public String pollId;
		
		public OptionViewParamaters() {
			
		}
		
		
		public OptionViewParamaters(String viewId, String id) {
			this.id=id;
			this.viewID = viewId;
		}

		public OptionViewParamaters(String viewId, String id, String pollId) {
			this.id=id;
			this.viewID = viewId;
			this.pollId = pollId;
		}
		
}
