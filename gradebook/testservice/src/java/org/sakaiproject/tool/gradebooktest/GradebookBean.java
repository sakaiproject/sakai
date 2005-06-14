package org.sakaiproject.tool.gradebooktest;

import java.io.*;
import java.util.*;

import javax.faces.event.ActionEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.service.gradebook.shared.GradebookService;

public class GradebookBean {
	private static final Log log = LogFactory.getLog(GradebookBean.class);

	private String uid;
	private boolean uidFound;
	private GradebookService gradebookService;

	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public boolean isUidFound() {
		return uidFound;
	}

	public void search(ActionEvent event) {
		uidFound = getGradebookService().gradebookExists(uid);
		log.info("search uid=" + uid + ", uidFound=" + uidFound);
	}

	public GradebookService getGradebookService() {
		log.info("getGradebookService " + gradebookService);
		return gradebookService;
	}
	public void setGradebookService(GradebookService gradebookService) {
		log.info("setGradebookService " + gradebookService);
		this.gradebookService = gradebookService;
	}

}
