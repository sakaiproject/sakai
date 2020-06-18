package org.sakaiproject.elfinder.controller.executors;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;

import cn.bluejoe.elfinder.controller.executor.AbstractCommandExecutor;
import cn.bluejoe.elfinder.controller.executor.CommandExecutor;
import cn.bluejoe.elfinder.controller.executor.FsItemEx;
import cn.bluejoe.elfinder.service.FsService;

public class SakaiTmbCommandExecutor extends AbstractCommandExecutor implements CommandExecutor {

    @Override
    public void execute(FsService fsService, HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws Exception {
        String target = request.getParameter("target");
        FsItemEx fsi = super.findItem(fsService, target);
        int width = fsService.getServiceConfig().getTmbWidth();
        try (InputStream is = fsi.openInputStream()) {
            ByteArrayOutputStream baos = resize(is, width);
            response.setHeader(HttpHeaders.LAST_MODIFIED, DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("GMT"))));
            response.setHeader(HttpHeaders.EXPIRES, DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("GMT")).plus(2, ChronoUnit.YEARS)));
            response.getOutputStream().write(baos.toByteArray());
        }
    }

    public ByteArrayOutputStream resize(InputStream inputStream , int size) throws Exception {
        BufferedImage originalImage = ImageIO.read(inputStream);
        int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        int scaledWidth;
        int scaledHeight;

        if (width > height) {
            scaledWidth = (width / height) * size;
            scaledHeight = size;
        } else if (width < height) {
            scaledWidth = size;
            scaledHeight = (height / width) * size;
        } else {
            scaledWidth = size;
            scaledHeight = size;
        }

        BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, type);
        Graphics2D g = scaledImage.createGraphics();
        g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();
        g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(scaledImage, "png", out);
        return out;
    }
}
