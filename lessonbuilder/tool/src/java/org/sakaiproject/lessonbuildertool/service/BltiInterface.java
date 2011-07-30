package org.sakaiproject.lessonbuildertool.service;

public interface BltiInterface {
    public boolean servicePresent();
    public boolean isPopUp();
    public int frameSize();
    public String doImportTool(String launchUrl, String bltiTitle, String strXml, String custom);
}

