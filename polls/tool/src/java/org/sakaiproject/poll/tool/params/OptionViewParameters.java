package org.sakaiproject.poll.tool.params;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class OptionViewParameters extends SimpleViewParameters {

		public String id;
		public String pollId;
		
		public OptionViewParameters() {
			
		}
		
		public OptionViewParameters(String viewId) {
			
		}
		
		public OptionViewParameters(String viewId, String id) {
			this.id=id;
			this.viewID = viewId;
		}

		public OptionViewParameters(String viewId, String id, String pollId) {
			this.id=id;
			this.viewID = viewId;
			this.pollId = pollId;
		}
		
}
