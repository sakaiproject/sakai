package cn.bluejoe.elfinder.controller.executors;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import cn.bluejoe.elfinder.controller.executor.AbstractJsonCommandExecutor;
import cn.bluejoe.elfinder.controller.executor.CommandExecutor;
import cn.bluejoe.elfinder.controller.executor.FsItemEx;
import cn.bluejoe.elfinder.service.FsItemFilter;
import cn.bluejoe.elfinder.service.FsService;

public class UploadCommandExecutor extends AbstractJsonCommandExecutor
		implements CommandExecutor
{
	@Override
	public void execute(FsService fsService, HttpServletRequest request,
			ServletContext servletContext, JSONObject json) throws Exception
	{
		List<FileItemInput> listFiles = (List<FileItemInput>) request
				.getAttribute(FileItemInput.class.getName());
		List<FsItemEx> added = new ArrayList<FsItemEx>();

		String target = request.getParameter("target");
		FsItemEx dir = super.findItem(fsService, target);

		FsItemFilter filter = getRequestedFilter(request);
		for (FileItemInput fis : listFiles)
		{
			//fis.getName() returns full path such as 'C:\temp\abc.txt' in IE10
			//while returns 'abc.txt' in Chrome
			//see https://github.com/bluejoe2008/elfinder-2.x-servlet/issues/22
			java.nio.file.Path p = java.nio.file.Paths.get(fis.getName());
		    FsItemEx newFile = new FsItemEx(dir, p.getFileName().toString());
			/*
		    String fileName = fis.getName();
			FsItemEx newFile = new FsItemEx(dir, fileName);
			*/
			newFile.createFile();
			InputStream is = fis.getInputStream();
			newFile.writeStream(is);
			if (filter.accepts(newFile))
				added.add(newFile);
		}

		json.put("added", files2JsonArray(request, added));
	}
}
