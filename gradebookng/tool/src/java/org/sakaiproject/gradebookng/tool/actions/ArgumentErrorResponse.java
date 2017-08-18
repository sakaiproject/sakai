package org.sakaiproject.gradebookng.tool.actions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ArgumentErrorResponse implements ActionResponse {
	private String msg;

	public ArgumentErrorResponse(final String msg) {
		this.msg = msg;
	}

	public String getStatus() {
		return "error";
	}

	public String toJson() {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode result = mapper.createObjectNode();

		result.put("msg", msg);

		return result.toString();
	}
}
