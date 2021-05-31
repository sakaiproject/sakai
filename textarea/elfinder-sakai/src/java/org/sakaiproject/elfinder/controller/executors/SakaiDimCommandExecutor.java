package org.sakaiproject.elfinder.controller.executors;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import cn.bluejoe.elfinder.controller.executor.AbstractJsonCommandExecutor;
import cn.bluejoe.elfinder.controller.executor.CommandExecutor;
import cn.bluejoe.elfinder.controller.executor.FsItemEx;
import cn.bluejoe.elfinder.service.FsService;

/**
 * This returns the dimensions on an image.
 */
@Slf4j
public class SakaiDimCommandExecutor extends AbstractJsonCommandExecutor implements CommandExecutor {

    @Override
    protected void execute(FsService fsService, HttpServletRequest request,
                           ServletContext servletContext, JSONObject json) throws Exception {
        String target = request.getParameter("target");
        FsItemEx item = findItem(fsService, target);
        // If it's not an image then just return empty JSON.
        if (item.getMimeType().startsWith("image")) {
			try (ImageInputStream iis = ImageIO.createImageInputStream(item.openInputStream())) {
				Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(iis);
				while (imageReaders.hasNext()) {
					ImageReader reader = imageReaders.next();
					try {
						reader.setInput(iis);
						int width = reader.getWidth(0);
						int height = reader.getHeight(0);

						if (width > 0 && height > 0) {
							json.put("dim", String.format("%dx%d", width, height));
							return;
						}
					} catch (Exception e) {
						log.warn("Failed load image to get dimensions: {}", item.getPath());
					} finally {
						reader.dispose();
					}
				}
            }
        } else {
            log.debug("dim command called on non-image: {}", item.getPath());
        }
    }
}
