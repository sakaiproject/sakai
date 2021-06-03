package org.sakaiproject.elfinder.controller.executors;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;

import cn.bluejoe.elfinder.controller.executor.AbstractCommandExecutor;
import cn.bluejoe.elfinder.controller.executor.CommandExecutor;
import cn.bluejoe.elfinder.controller.executor.FsItemEx;
import cn.bluejoe.elfinder.service.FsService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SakaiTmbCommandExecutor extends AbstractCommandExecutor implements CommandExecutor {

    public static final long MAX_TMB_SIZE_BYTES = 20971520;
    public static final long MAX_IMG_BYTES_TO_LOAD = 104857600;

    static {
        // https://bugs.openjdk.java.net/browse/JDK-6986863
        // Fixed in JDK 17
        ICC_Profile.getInstance(ColorSpace.CS_sRGB).getData();
        ICC_Profile.getInstance(ColorSpace.CS_PYCC).getData();
        ICC_Profile.getInstance(ColorSpace.CS_GRAY).getData();
        ICC_Profile.getInstance(ColorSpace.CS_CIEXYZ).getData();
        ICC_Profile.getInstance(ColorSpace.CS_LINEAR_RGB).getData();
    }

    @Override
    public void execute(FsService fsService, HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws Exception {
        String target = request.getParameter("target");
        FsItemEx fsi = super.findItem(fsService, target);
        if (fsi.getSize() < MAX_TMB_SIZE_BYTES) { // image must be less than MAX_TMB_SIZE_BYTES
            int tmbWidth = fsService.getServiceConfig().getTmbWidth();
            try (InputStream inputStream = fsi.openInputStream()) {
                ByteArrayOutputStream baos = resize(inputStream, tmbWidth);
                if (baos != null) {
                    response.setHeader(HttpHeaders.LAST_MODIFIED, DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("GMT"))));
                    response.setHeader(HttpHeaders.EXPIRES, DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("GMT")).plus(2, ChronoUnit.YEARS)));
                    response.getOutputStream().write(baos.toByteArray());
                }
            }
        }
    }

    public ByteArrayOutputStream resize(InputStream inputStream, int size) throws Exception {
        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
            Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageInputStream);
            ImageReader reader = imageReaders.next();
            reader.setInput(imageInputStream);

            int width = reader.getWidth(0);
            int height = reader.getHeight(0);
            if ((width * height * 4) > MAX_IMG_BYTES_TO_LOAD) {
                return null;
            }

            // Now can load the whole image
            BufferedImage originalImage = reader.read(0);
            int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
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

            originalImage.flush();
            scaledImage.flush();

            return out;
        }
    }
}
