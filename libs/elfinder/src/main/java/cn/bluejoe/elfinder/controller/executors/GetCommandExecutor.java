package cn.bluejoe.elfinder.controller.executors;

import java.io.InputStream;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import cn.bluejoe.elfinder.controller.executor.AbstractJsonCommandExecutor;
import cn.bluejoe.elfinder.controller.executor.CommandExecutor;
import cn.bluejoe.elfinder.controller.executor.FsItemEx;
import cn.bluejoe.elfinder.service.FsService;

public class GetCommandExecutor extends AbstractJsonCommandExecutor implements CommandExecutor
{
	@Override
	public void execute(FsService fsService, HttpServletRequest request, ServletContext servletContext, JSONObject json)
			throws Exception
	{
		String target = request.getParameter("target");

		FsItemEx fsi = super.findItem(fsService, target);
		InputStream is = fsi.openInputStream();
		String content = IOUtils.toString(is, "utf-8");
		is.close();
		json.put("content", content);
	}
}
