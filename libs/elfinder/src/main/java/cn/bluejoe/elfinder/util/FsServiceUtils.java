package cn.bluejoe.elfinder.util;

import java.io.IOException;

import cn.bluejoe.elfinder.controller.executor.FsItemEx;
import cn.bluejoe.elfinder.service.FsItem;
import cn.bluejoe.elfinder.service.FsService;

public abstract class FsServiceUtils
{
	public static FsItemEx findItem(FsService fsService, String hash) throws IOException
	{
		FsItem fsi = fsService.fromHash(hash);
		if (fsi == null)
		{
			return null;
		}

		return new FsItemEx(fsi, fsService);
	}
}
