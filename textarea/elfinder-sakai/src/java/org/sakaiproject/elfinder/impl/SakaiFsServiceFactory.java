package org.sakaiproject.elfinder.impl;

import cn.bluejoe.elfinder.service.FsService;
import cn.bluejoe.elfinder.service.FsServiceFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by buckett on 08/07/15.
 */
public class SakaiFsServiceFactory implements FsServiceFactory {

    public void setFsService(FsService fsService) {
        this.fsService = fsService;
    }

    protected FsService fsService;

    public FsService getFileService(HttpServletRequest request, ServletContext servletContext) {
        return fsService;
    }
}
