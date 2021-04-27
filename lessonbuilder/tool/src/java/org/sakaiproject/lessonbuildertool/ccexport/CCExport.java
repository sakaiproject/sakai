/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 * <p>
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://opensource.org/licenses/ecl2
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.lessonbuildertool.ccexport;

import static org.sakaiproject.lessonbuildertool.ccexport.CCVersion.V11;
import static org.sakaiproject.lessonbuildertool.ccexport.CCVersion.V12;
import static org.sakaiproject.lessonbuildertool.ccexport.CCVersion.V13;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.util.ResourceLoaderMessageSource;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.PreferencesService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CCExport {

    @Setter private AssignmentExport assignmentExport;
    @Setter private BLTIExport bltiExport;
    @Setter private CCUtils ccUtils;
    @Setter private ContentHostingService contentHostingService;
    @Setter private ForumsExport forumsExport;
    @Setter private LessonsExport lessonsExport;
    @Setter private PreferencesService preferencesService;
    @Setter private SamigoExport samigoExport;
    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private SessionManager sessionManager;
    @Setter private SimplePageToolDao simplePageToolDao;
    @Setter private SiteService siteService;

    private ResourceLoaderMessageSource messageSource;

    public CCExport() {
        messageSource = new ResourceLoaderMessageSource();
        messageSource.setBasename("messages");
    }

    private void setErrMessage(String message) {
        ToolSession toolSession = sessionManager.getCurrentToolSession();
        if (toolSession == null) {
            log.warn("Lesson Builder error not in tool: {}", message);
            return;
        }
        List<String> errors = Optional.ofNullable((List<String>) toolSession.getAttribute("lessonbuilder.errors")).orElseGet(ArrayList::new);
        errors.add(message);
        toolSession.setAttribute("lessonbuilder.errors", errors);
    }

    private void setErrKey(String key, String text, Locale locale) {
        String trimmedText = StringUtils.trimToEmpty(text);
        String message = messageSource.getMessage(key, null, locale);
        setErrMessage(message.replace("{}", trimmedText));
    }

    /*
     * maintain global lists of resources, adding as they are referenced on a page or
     * adding all resources of a kind, depending. Each type of resource has a map
     * indexed by sakai ID, with a generated ID for the cartridge and the name of the
     * file or XML file.
     *
     * the overall flow will be to load all resources and tests into the temp directory
     * and the maps, the walk the lesson hierarchy building imsmanifest.xml. Any resources
     * not used will get some kind of dummy entries in imsmanifest.xml, so that the whole
     * contents of the site is brought over.
     */

    public void doExport(HttpServletResponse response, String siteId, String version, String bank) {
        CCConfig ccConfig = new CCConfig(siteId, preferencesService.getLocale(sessionManager.getCurrentSessionUserId()));

        if ("1.1".equals(version)) {
            ccConfig.setVersion(V11);
        } else if ("1.3".equals(version)) {
            ccConfig.setVersion(V13);
        }

        if ("1".equals(bank)) {
            ccConfig.setDoBank(true);
        }

        ccConfig.setResults(new ArrayList<>());

        try {
            if (!addAllFiles(ccConfig)) return;
            if (!addAllSamigo(ccConfig)) return;
            if (!addAllAssignments(ccConfig)) return;
            if (!addAllForums(ccConfig)) return;
            if (!addAllBlti(ccConfig)) return;
        } catch (Exception e) {
            log.error("Lessons export error outputting file, {}", e.toString());
            setErrKey("simplepage.exportcc-fileerr", e.getMessage(), ccConfig.getLocale());
        }

        try (ZipPrintStream out = new ZipPrintStream(response.getOutputStream())) {
            out.setLevel(serverConfigurationService.getInt("zip.compression.level", 1));
            response.setHeader("Content-disposition", "inline; filename=sakai-export.imscc");
            response.setContentType("application/zip");

            outputAllFiles(ccConfig, out);
            outputAllSamigo(ccConfig, out);
            outputAllAssignments(ccConfig, out);
            outputAllForums(ccConfig, out);
            outputAllBlti(ccConfig, out);
            outputAllTexts(ccConfig, out);
            outputManifest(ccConfig, out);

            ZipEntry zipEntry = new ZipEntry("cc-objects/export-errors.txt");
            out.putNextEntry(zipEntry);
            ccConfig.getResults().forEach(out::println);
        } catch (IOException ioe) {
            log.error("Lessons export error streaming file, {}", ioe.toString());
            setErrKey("simplepage.exportcc-fileerr", ioe.getMessage(), ccConfig.getLocale());
        }
    }

    public boolean addAllFiles(CCConfig ccConfig) {
        try {
            String base = contentHostingService.getSiteCollection(ccConfig.getSiteId());
            ContentCollection baseCol = contentHostingService.getCollection(base);
            return addAllFiles(ccConfig, baseCol, base.length());
        } catch (IdUnusedException e) {
            return true;
        } catch (Exception e) {
            log.error("Lessons export error outputting file, addAllFiles " + e);
            setErrKey("simplepage.exportcc-fileerr", e.getMessage(), ccConfig.getLocale());
            return false;
        }

    }

    public boolean addAllFiles(CCConfig ccConfig, ContentCollection baseCol, int baselen) {
        try {

            List<ContentEntity> members = baseCol.getMemberResources();
            for (ContentEntity entity : members) {

                // don't export things we generate. Can lead to collisions
                String filename = entity.getId().substring(baselen);
                if ("cc-objects/export-errors.txt".equals(filename) || "cc-objects".equals(filename))
                    continue;

                if (entity instanceof ContentResource) {
                    boolean aLink = ccUtils.isLink((ContentResource) entity);
                    String location;
                    if (aLink) {
                        location = "attachments/" + ccConfig.getResourceIdPeek() + ".xml";
                        String url = new String(((ContentResource) entity).getContent());
                        // see if Youtube. If so, use the current recommended URL
                        String youtubeKey = SimplePageBean.getYoutubeKeyFromUrl(url);
                        if (youtubeKey != null) {
							// code is also in ShowPageProducer. keep in sync
							url = SimplePageBean.getYoutubeUrlFromKey(youtubeKey);
						}
                        CCResourceItem res = ccConfig.addFile(entity.getId(), location);
                        res.setLink(true);
                        res.setUrl(url);
                        // try to get title from resource. Will normally be the URL
                        res.setTitle(entity.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME));
                        if (StringUtils.isBlank(res.getTitle())) res.setTitle(url);
                        // queue this so we output the XML file
                        ccConfig.getLinkSet().add(res);
                    } else {
                        location = entity.getId().substring(baselen);
                        ccConfig.addFile(entity.getId(), location);
                    }
                } else {
					addAllFiles(ccConfig, (ContentCollection) entity, baselen);
				}
            }
        } catch (Exception e) {
            log.warn("Lessons export error outputting file, addAllFiles 2 {}", e.toString());
            setErrKey("simplepage.exportcc-fileerr", e.getMessage(), ccConfig.getLocale());
            return false;
        }
        return true;
    }

    public void outputAllFiles(CCConfig ccConfig, ZipPrintStream out) {
        try {
            for (Map.Entry<String, CCResourceItem> entry : ccConfig.getFileMap().entrySet()) {

                // normally this is a file ID for contenthosting.
                // But jforum gives us an actual filesystem filename. We stick /// on
                // the front to make that clear. inSakai is contenthosting.
                boolean inSakai = !entry.getKey().startsWith("///");

                ZipEntry zipEntry = new ZipEntry(entry.getValue().getLocation());

                // for contenthosting
                ContentResource resource = null;
                // for raw file
                File infile = null;
                InputStream instream = null;
                if (inSakai) {
                    resource = contentHostingService.getResource(entry.getKey());
                    // if URL there's no file to output. The link XML file will
                    // be done at the end, since some links are discovered while outputting manifest
                    if (entry.getValue().isLink()) {
                        continue;
                    } else {
						zipEntry.setSize(resource.getContentLength());
					}
                } else {
                    infile = new File(entry.getKey().substring(3));
                    instream = new FileInputStream(infile);
                }

                out.putNextEntry(zipEntry);

                // see if this is HTML. If so, we need to scan it.
                String filename = entry.getKey();
                int lastdot = filename.lastIndexOf(".");
                int lastslash = filename.lastIndexOf("/");

                String extension = "";
                if (lastdot >= 0 && lastdot > lastslash) extension = filename.substring(lastdot + 1);

                String mimeType = null;
                if (inSakai) mimeType = resource.getContentType();

                boolean isHtml = false;
                if (StringUtils.isBlank(mimeType) || mimeType.startsWith("http")) mimeType = null;
                if (StringUtils.equalsAny(mimeType,"text/html", "application/xhtml+xml")
                        || mimeType == null && StringUtils.equalsAny(extension, "html", "htm")) {
                    isHtml = true;
                }

                InputStream contentStream = null;
                try {
                    if (isHtml) {
                        // treat html separately. Need to convert urls to relative
                        String content = null;
                        if (inSakai) {
                            content = new String(resource.getContent());
                        } else {
                            byte[] b = new byte[(int) infile.length()];
                            instream.read(b);
                            content = new String(b);
                        }
                        content = ccUtils.relFixup(ccConfig, content, entry.getValue());
                        out.print(content);
                    } else {
                        if (inSakai) {
                            contentStream = resource.streamContent();
                        } else {
                            contentStream = instream;
                        }
                        IOUtils.copyLarge(contentStream, out);
                    }
                } catch (Exception e) {
                    log.error("Lessons export error outputting file " + e);
                } finally {
                    IOUtils.closeQuietly(contentStream);
                    IOUtils.closeQuietly(instream);
                }

            }
        } catch (Exception e) {
            log.error("Lessons export error outputting file, outputAllFiles " + e);
            setErrKey("simplepage.exportcc-fileerr", e.getMessage(), ccConfig.getLocale());
        }
    }

    public boolean addAllSamigo(CCConfig ccConfig) {

        List<String> tests = samigoExport.getEntitiesInSite(ccConfig.getSiteId());
        if (tests == null) return true;

        // These are going to be loaded into the final file system. I considered
        // putting them in a separate directory to avoid conflicting with real files.
        // However this would force all URLs to be written with ../ at the start,
        // which is probably more dangerous, as it depends upon loaders making the
        // same interpretation of a somewhat ambiguous specification.
        for (String sakaiId : tests) {
            CCResourceItem res = new CCResourceItem(sakaiId, ccConfig.getResourceId(), "cc-objects/" + ccConfig.getResourceId() + ".xml", null, null, null);
            ccConfig.getSamigoMap().put(sakaiId, res);
        }
        List<Long> poolIds = samigoExport.getAllPools();
        if (ccConfig.isDoBank() && !poolIds.isEmpty()) {
            if (ccConfig.getVersion().equals(V13)) {
                for (Long poolId : poolIds) {
                    CCResourceItem res = new CCResourceItem(null, ccConfig.getResourceId(), "cc-objects/" + ccConfig.getResourceId() + ".xml", null, null, null);
                    ccConfig.getPoolMap().put(poolId, res);
                }
            } else {
                CCResourceItem res = new CCResourceItem(null, ccConfig.getResourceId(), "cc-objects/" + ccConfig.getResourceId() + ".xml", null, null, null);
                ccConfig.getPoolMap().put(1L, res);
            }
        }
        return true;
    }

    public void outputAllSamigo(CCConfig ccConfig, ZipPrintStream out) {
        try {
            for (Map.Entry<String, CCResourceItem> entry : ccConfig.getSamigoMap().entrySet()) {

                ZipEntry zipEntry = new ZipEntry(entry.getValue().getLocation());
                out.putNextEntry(zipEntry);
                boolean ok = samigoExport.outputEntity(ccConfig, entry.getValue().getSakaiId(), out, entry.getValue(), ccConfig.getVersion());
                if (!ok) return;
            }
            if (!ccConfig.getPoolMap().isEmpty()) {
                for (Map.Entry<Long, CCResourceItem> entry : ccConfig.getPoolMap().entrySet()) {
                    ZipEntry zipEntry = new ZipEntry(entry.getValue().getLocation());
                    out.putNextEntry(zipEntry);
                    boolean ok = samigoExport.outputBank(ccConfig, entry.getKey(), out, ccConfig.getSamigoBank(), ccConfig.getVersion());
                    if (!ok) return;
                }
            }
        } catch (Exception e) {
            log.error("output sam " + e);
            setErrKey("simplepage.exportcc-fileerr", e.getMessage(), ccConfig.getLocale());
        }
    }

    public boolean addAllAssignments(CCConfig ccConfig) {
        List<String> assignments = assignmentExport.getEntitiesInSite(ccConfig);
        if (assignments == null) {
            return true;
        }
        for (String sakaiId : assignments) {
            int slash = sakaiId.indexOf("/");
            CCResourceItem res = new CCResourceItem(sakaiId, ccConfig.getResourceId(), "attachments/" + sakaiId.substring(slash + 1) + "/assignmentpage.html", null, null, null);
            ccConfig.getAssignmentMap().put(sakaiId, res);
        }

        return true;
    }

    private void outputAllAssignments(CCConfig ccConfig, ZipPrintStream out) {
        try {
            for (Map.Entry<String, CCResourceItem> entry : ccConfig.getAssignmentMap().entrySet()) {

                ZipEntry zipEntry = new ZipEntry(entry.getValue().getLocation());

                out.putNextEntry(zipEntry);
                boolean ok = assignmentExport.outputEntity(ccConfig, entry.getValue().getSakaiId(), out, entry.getValue());
                if (!ok) return;

                if (ccConfig.getVersion().greaterThanOrEqualTo(V13)) {
                    String xmlHref = "cc-objects/" + entry.getValue().getResourceId() + ".xml";

                    zipEntry = new ZipEntry(xmlHref);

                    out.putNextEntry(zipEntry);
                    ok = assignmentExport.outputEntity2(ccConfig, entry.getValue().getSakaiId(), out, entry.getValue());
                    if (!ok) return;
                }
            }

        } catch (Exception e) {
            log.error("Lessons export error outputting file, {}" + e.toString());
            setErrKey("simplepage.exportcc-fileerr", e.getMessage(), ccConfig.getLocale());
        }
    }

    public boolean addAllForums(CCConfig ccConfig) {
        List<String> forums = forumsExport.getEntitiesInSite(ccConfig);
        if (forums == null) return true;
        for (String sakaiId : forums) {
            String resourceId = ccConfig.getResourceId();
            CCResourceItem res = new CCResourceItem(sakaiId, resourceId, "cc-objects/" + resourceId + ".xml", null, null, null);
            ccConfig.getForumsMap().put(sakaiId, res);
        }
        return true;
    }

    public void outputAllForums(CCConfig ccConfig, ZipPrintStream out) {
        try {
            for (Map.Entry<String, CCResourceItem> entry : ccConfig.getForumsMap().entrySet()) {
                ZipEntry zipEntry = new ZipEntry(entry.getValue().getLocation());

                out.putNextEntry(zipEntry);
                boolean ok = forumsExport.outputEntity(ccConfig, entry.getValue().getSakaiId(), out, entry.getValue(), ccConfig.getVersion());
                if (!ok) return;
            }
        } catch (Exception e) {
            log.error("problem in outputallforums, outputAllForums " + e);
            setErrKey("simplepage.exportcc-fileerr", e.getMessage(), ccConfig.getLocale());
        }
    }

    public boolean addAllBlti(CCConfig ccConfig) {
        List<String> bltis = bltiExport.getEntitiesInSite(ccConfig.getSiteId());
        if (bltis == null) return true;
        for (String sakaiId : bltis) {
            String resourceId = ccConfig.getResourceId();
            CCResourceItem res = new CCResourceItem(sakaiId, resourceId, "cc-objects/" + resourceId + ".xml", null, null, null);
            ccConfig.getBltiMap().put(sakaiId, res);
        }
        return true;
    }

    public void outputAllBlti(CCConfig ccConfig, ZipPrintStream out) {
        try {
            for (Map.Entry<String, CCResourceItem> entry : ccConfig.getBltiMap().entrySet()) {
                ZipEntry zipEntry = new ZipEntry(entry.getValue().getLocation());

                out.putNextEntry(zipEntry);
                boolean ok = bltiExport.outputEntity(ccConfig.getSiteId(), entry.getValue().getSakaiId(), out, ccConfig.getVersion());
                if (!ok) return;
            }
        } catch (Exception e) {
            log.error("problem in outputallforums, {}" + e.toString());
            setErrKey("simplepage.exportcc-fileerr", e.getMessage(), ccConfig.getLocale());
        }
    }

    public void outputAllTexts(CCConfig ccConfig, ZipPrintStream out) {
        try {
            List<SimplePageItem> items = simplePageToolDao.findTextItemsInSite(ccConfig.getSiteId());

            for (SimplePageItem item : items) {
                String location = "attachments/item-" + item.getId() + ".html";

                ZipEntry zipEntry = new ZipEntry(location);
                out.putNextEntry(zipEntry);

                String sakaiId = "/text/" + item.getId();
                CCResourceItem res = new CCResourceItem(sakaiId, ccConfig.getResourceId(), location, null, null, null);
                ccConfig.getFileMap().put(sakaiId, res);

                out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
                out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">");
                out.println("<body>");
                out.print(ccUtils.relFixup(ccConfig, item.getHtml(), res, new StringBuilder()));
                out.println("</body>");
                out.println("</html>");
            }
        } catch (Exception e) {
            log.error("Lessons export error outputting file, {}" + e.toString());
            setErrKey("simplepage.exportcc-fileerr", e.getMessage(), ccConfig.getLocale());
        }
    }

    public boolean outputLessons(CCConfig ccConfig, ZipPrintStream out) {
        out.println("  <organization identifier=\"page\" structure=\"rooted-hierarchy\">");
        out.println("    <item identifier=\"I_1\">");
        List<SimplePageItem> sitePages = simplePageToolDao.findItemsInSite(ccConfig.getSiteId());

        sitePages.forEach(i -> ccConfig.getPagesDone().add(Long.valueOf(i.getSakaiId())));
        sitePages.forEach(i -> lessonsExport.outputLessonPage(ccConfig, out, Long.valueOf(i.getSakaiId()), i.getName(), 6, true));
        out.println("    </item>");
        out.println("  </organization>");
        return true;
    }

    public void outputManifest(CCConfig ccConfig, ZipPrintStream out) {

        String title;
        Site site;
        try {
            site = siteService.getSite(ccConfig.getSiteId());
            title = site.getTitle();
        } catch (IdUnusedException e) {
            title = "SITE UNKNOWN";
        }

        try {
            ZipEntry zipEntry = new ZipEntry("imsmanifest.xml");
            out.putNextEntry(zipEntry);
            switch (ccConfig.getVersion()) {
                case V11:
                    out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                    out.println("<manifest identifier=\"cctd0001\"");
                    out.println("  xmlns=\"http://www.imsglobal.org/xsd/imsccv1p1/imscp_v1p1\"");
                    out.println("  xmlns:lom=\"http://ltsc.ieee.org/xsd/imsccv1p1/LOM/resource\"");
                    out.println("  xmlns:lomimscc=\"http://ltsc.ieee.org/xsd/imsccv1p1/LOM/manifest\"");
                    out.println("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
                    out.println("  xsi:schemaLocation=\"");
                    out.println("  http://ltsc.ieee.org/xsd/imsccv1p1/LOM/resource http://www.imsglobal.org/profile/cc/ccv1p1/LOM/ccv1p1_lomresource_v1p0.xsd ");
                    out.println("  http://www.imsglobal.org/xsd/imsccv1p1/imscp_v1p1 http://www.imsglobal.org/profile/cc/ccv1p1/ccv1p1_imscp_v1p2_v1p0.xsd ");
                    out.println("  http://ltsc.ieee.org/xsd/imsccv1p1/LOM/manifest http://www.imsglobal.org/profile/cc/ccv1p1/LOM/ccv1p1_lommanifest_v1p0.xsd\">");
                    out.println("  <metadata>");
                    out.println("    <schema>IMS Common Cartridge</schema>");
                    out.println("    <schemaversion>1.1.0</schemaversion>");
                    out.println("    <lomimscc:lom>");
                    out.println("      <lomimscc:general>");
                    out.println("        <lomimscc:title>");
                    out.println("          <lomimscc:string>" + StringEscapeUtils.escapeXml11(title) + "</lomimscc:string>");
                    out.println("        </lomimscc:title>");
                    out.println("      </lomimscc:general>");
                    out.println("    </lomimscc:lom>");
                    out.println("  </metadata>");
                    break;

                case V13:
                    out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                    out.println("<manifest identifier=\"cctd0001\"");
                    out.println("  xmlns=\"http://www.imsglobal.org/xsd/imsccv1p3/imscp_v1p1\"");
                    out.println("  xmlns:lom=\"http://ltsc.ieee.org/xsd/imsccv1p3/LOM/resource\"");
                    out.println("  xmlns:lomimscc=\"http://ltsc.ieee.org/xsd/imsccv1p3/LOM/manifest\"");
                    out.println("  xmlns:cpx=\"http://www.imsglobal.org/xsd/imsccv1p3/imscp_extensionv1p2\"");
                    out.println("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
                    out.println("  xsi:schemaLocation=\"http://ltsc.ieee.org/xsd/imsccv1p3/LOM/resource http://www.imsglobal.org/profile/cc/ccv1p3/LOM/ccv1p3_lomresource_v1p0.xsd ");
                    out.println("                       http://www.imsglobal.org/xsd/imsccv1p3/imscp_v1p1 http://www.imsglobal.org/profile/cc/ccv1p3/ccv1p3_imscp_v1p2_v1p0.xsd ");
                    out.println("                       http://ltsc.ieee.org/xsd/imsccv1p3/LOM/manifest http://www.imsglobal.org/profile/cc/ccv1p3/LOM/ccv1p3_lommanifest_v1p0.xsd ");
                    out.println("                       http://www.imsglobal.org/xsd/imsccv1p3/imscp_extensionv1p2 http://www.imsglobal.org/profile/cc/ccv1p3/ccv1p3_cpextensionv1p2_v1p0.xsd\">");
                    out.println("  <metadata>");
                    out.println("    <schema>IMS Common Cartridge</schema>");
                    out.println("    <schemaversion>1.3.0</schemaversion>");
                    out.println("    <lomimscc:lom>");
                    out.println("      <lomimscc:general>");
                    out.println("        <lomimscc:title>");
                    out.println("          <lomimscc:string>" + StringEscapeUtils.escapeXml11(title) + "</lomimscc:string>");
                    out.println("        </lomimscc:title>");
                    out.println("      </lomimscc:general>");
                    out.println("    </lomimscc:lom>");
                    out.println("  </metadata>");
                    break;

                default:
                    out.print(
                            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<manifest identifier=\"sakai1\"\n  xmlns=\"http://www.imsglobal.org/xsd/imsccv1p2/imscp_v1p1\"\nxmlns:lom=\"http://ltsc.ieee.org/xsd/imsccv1p2/LOM/resource\"\nxmlns:lomimscc=\"http://ltsc.ieee.org/xsd/imsccv1p2/LOM/manifest\"\nxmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\nxsi:schemaLocation=\"                                                                                                                        \n  http://ltsc.ieee.org/xsd/imsccv1p2/LOM/resource http://www.imsglobal.org/profile/cc/ccv1p2/LOM/ccv1p2_lomresource_v1p0.xsd                  \n  http://www.imsglobal.org/xsd/imsccv1p2/imscp_v1p1 http://www.imsglobal.org/profile/cc/ccv1p2/ccv1p2_imscp_v1p2_v1p0.xsd                     \n  http://ltsc.ieee.org/xsd/imsccv1p2/LOM/manifest http://www.imsglobal.org/profile/cc/ccv1p2/LOM/ccv1p2_lommanifest_v1p0.xsd\">\n  <metadata>\n    <schema>IMS Common Cartridge</schema>\n    <schemaversion>1.2.0</schemaversion>\n    <lomimscc:lom>\n      <lomimscc:general>\n	<lomimscc:title>\n	  <lomimscc:string>" + StringEscapeUtils.escapeXml11(title) + "</lomimscc:string>\n	</lomimscc:title>\n      </lomimscc:general>\n    </lomimscc:lom>\n  </metadata>\n ");
                    break;
            }

            out.println("  <organizations>");
            outputLessons(ccConfig, out);
            out.println("  </organizations>");

            String qtiid;
            String bankid;
            String topicid;
            String linkid;
            String usestr;
            switch (ccConfig.getVersion()) {
                case V11:
                    qtiid = "imsqti_xmlv1p2/imscc_xmlv1p1/assessment";
                    bankid = "imsqti_xmlv1p2/imscc_xmlv1p1/question-bank";
                    topicid = "imsdt_xmlv1p1";
                    linkid = "imswl_xmlv1p1";
                    usestr = "";
                    break;
                case V13:
                    qtiid = "imsqti_xmlv1p2/imscc_xmlv1p3/assessment";
                    bankid = "imsqti_xmlv1p2/imscc_xmlv1p3/question-bank";
                    topicid = "imsdt_xmlv1p3";
                    linkid = "imswl_xmlv1p3";
                    usestr = " intendeduse=\"assignment\"";
                    break;
                default:
                    qtiid = "imsqti_xmlv1p2/imscc_xmlv1p2/assessment";
                    bankid = "imsqti_xmlv1p2/imscc_xmlv1p2/question-bank";
                    topicid = "imsdt_xmlv1p2";
                    linkid = "imswl_xmlv1p2";
                    usestr = " intendeduse=\"assignment\"";
                    break;
            }

            out.println("  <resources>");
            for (Map.Entry<String, CCResourceItem> entry : ccConfig.getFileMap().entrySet()) {
                String use = "";
                if (ccConfig.getVersion().greaterThanOrEqualTo(V12) && entry.getValue().getUse() != null) {
                    use = " intendeduse=\"" + entry.getValue().getUse() + "\"";
                }
                String type = "webcontent";
                if (entry.getValue().isLink()) type = linkid;
                out.println("    <resource href=\"" + StringEscapeUtils.escapeXml11(entry.getValue().getLocation()) + "\" identifier=\"" + entry.getValue().getResourceId() + "\" type=\"" + type + "\"" + use + ">");
                out.println("      <file href=\"" + StringEscapeUtils.escapeXml11(entry.getValue().getLocation()) + "\"/>");
                entry.getValue().getDependencies().forEach(d -> out.println("      <dependency identifierref=\"" + d + "\"/>"));
                out.println("    </resource>");
            }

            for (Map.Entry<String, CCResourceItem> entry : ccConfig.getSamigoMap().entrySet()) {
                out.println("    <resource href=\"" + StringEscapeUtils.escapeXml11(entry.getValue().getLocation()) + "\" identifier=\"" + entry.getValue().getResourceId() + "\" type=\"" + qtiid + "\">");
                out.println("      <file href=\"" + StringEscapeUtils.escapeXml11(entry.getValue().getLocation()) + "\"/>");
                entry.getValue().getDependencies().forEach(d -> out.println("      <dependency identifierref=\"" + d + "\"/>"));
                out.println("    </resource>");
            }

            // question bank
            for (Map.Entry<Long, CCResourceItem> entry : ccConfig.getPoolMap().entrySet()) {
                out.println("    <resource href=\"" + StringEscapeUtils.escapeXml11(entry.getValue().getLocation()) + "\" identifier=\"" + entry.getValue().getResourceId() + "\" type=\"" + bankid + "\">");
                out.println("      <file href=\"" + StringEscapeUtils.escapeXml11(entry.getValue().getLocation()) + "\"/>");
                entry.getValue().getDependencies().forEach(d -> out.println("      <dependency identifierref=\"" + d + "\"/>"));
                out.println("    </resource>");
            }

            for (Map.Entry<String, CCResourceItem> entry : ccConfig.getAssignmentMap().entrySet()) {
                String variantId = null;
                out.println("    <resource href=\"" + StringEscapeUtils.escapeXml11(entry.getValue().getLocation()) + "\" identifier=\"" + entry.getValue().getResourceId() + "\" type=\"webcontent\"" + usestr + ">");
                out.println("      <file href=\"" + StringEscapeUtils.escapeXml11(entry.getValue().getLocation()) + "\"/>");
                entry.getValue().getDependencies().forEach(d -> out.println("      <dependency identifierref=\"" + d + "\"/>"));
                if (ccConfig.getVersion().greaterThanOrEqualTo(V13)) {
                    variantId = ccConfig.getResourceId();
                    out.println("      <cpx:variant identifier=\"" + ccConfig.getResourceId() + "\" identifierref=\"" + variantId + "\">");
                    out.println("        <cpx:metadata/>");
                    out.println("      </cpx:variant>");
                }
                out.println("    </resource>");

                // output the preferred version for 1.3 and up
                if (ccConfig.getVersion().greaterThanOrEqualTo(V13)) {
                    String xmlHref = "cc-objects/" + entry.getValue().getResourceId() + ".xml";
                    out.println("    <resource href=\"" + StringEscapeUtils.escapeXml11(xmlHref) + "\" identifier=\"" + variantId + "\" type=\"assignment_xmlv1p0\">");
                    out.println("      <file href=\"" + StringEscapeUtils.escapeXml11(xmlHref) + "\"/>");
                    entry.getValue().getDependencies().forEach(d -> out.println("      <dependency identifierref=\"" + d + "\"/>"));
                    out.println("    </resource>");
                }
            }

            for (Map.Entry<String, CCResourceItem> entry : ccConfig.getForumsMap().entrySet()) {
                out.println("    <resource href=\"" + StringEscapeUtils.escapeXml11(entry.getValue().getLocation()) + "\" identifier=\"" + entry.getValue().getResourceId() + "\" type=\"" + topicid + "\">");
                out.println("      <file href=\"" + StringEscapeUtils.escapeXml11(entry.getValue().getLocation()) + "\"/>");
                entry.getValue().getDependencies().forEach(d -> out.println("      <dependency identifierref=\"" + d + "\"/>"));
                out.println("    </resource>");
            }

            for (Map.Entry<String, CCResourceItem> entry : ccConfig.getBltiMap().entrySet()) {
                out.println("    <resource href=\"" + StringEscapeUtils.escapeXml11(entry.getValue().getLocation()) + "\" identifier=\"" + entry.getValue().getResourceId() + "\" type=\"imsbasiclti_xmlv1p0\">");
                out.println("      <file href=\"" + StringEscapeUtils.escapeXml11(entry.getValue().getLocation()) + "\"/>");
                entry.getValue().getDependencies().forEach(d -> out.println("      <dependency identifierref=\"" + d + "\"/>"));
                out.println("    </resource>");
            }

            // add error log at the very end
            String errId = ccConfig.getResourceId();

            out.println(("    <resource href=\"cc-objects/export-errors.txt\" identifier=\"" + errId + "\" type=\"webcontent\">\n      <file href=\"cc-objects/export-errors.txt\"/>\n    </resource>"));
            out.println("  </resources>\n</manifest>");

            // items with embed code. need to put out the HTML page
            for (Map.Entry<Long, String> entry : ccConfig.getEmbedMap().entrySet()) {
                Long itemId = entry.getKey();
                String location = "attachments/item-" + itemId + ".html";

                ZipEntry ze = new ZipEntry(location);
                out.putNextEntry(ze);

                out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
                out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">");
                out.println("<body>");
                out.print(entry.getValue());
                out.println("</body>");
                out.println("</html>");
            }

            // links. need to put out the XML file defining the link
            for (CCResourceItem res : ccConfig.getLinkSet()) {
                ZipEntry ze = new ZipEntry(res.getLocation());
                out.putNextEntry(ze);
                switch (ccConfig.getVersion()) {
                    case V11:
                        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                        out.println("<webLink xmlns=\"http://www.imsglobal.org/xsd/imsccv1p1/imswl_v1p1\"");
                        out.println("      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
                        out.println("      xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imsccv1p1/imswl_v1p1 http://www.imsglobal.org/profile/cc/ccv1p1/ccv1p1_imswl_v1p1.xsd\">");
                        out.println("  <title>" + StringEscapeUtils.escapeXml11(res.getTitle()) + "</title>");
                        out.println("  <url href=\"" + StringEscapeUtils.escapeXml11(res.getUrl()) + "\"/>");
                        out.println("</webLink>");
                        break;
                    case V13:
                        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                        out.println("<webLink xmlns=\"http://www.imsglobal.org/xsd/imsccv1p3/imswl_v1p3\"");
                        out.println("      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imsccv1p3/imswl_v1p3 ");
                        out.println("      http://www.imsglobal.org/profile/cc/ccv1p3/ccv1p3_imswl_v1p3.xsd\">");
                        out.println("  <title>" + StringEscapeUtils.escapeXml11(res.getTitle()) + "</title>");
                        out.println("  <url href=\"" + StringEscapeUtils.escapeXml11(res.getUrl()) + "\"/>");
                        out.println("</webLink>");
                        break;
                    default:
                        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                        out.println("<webLink xmlns=\"http://www.imsglobal.org/xsd/imsccv1p2/imswl_v1p2\"");
                        out.println("      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
                        out.println("      xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imsccv1p2/imswl_v1p2 http://www.imsglobal.org/profile/cc/ccv1p2/ccv1p2_imswl_v1p2.xsd\">");
                        out.println("  <title>" + StringEscapeUtils.escapeXml11(res.getTitle()) + "</title>");
                        out.println("  <url href=\"" + StringEscapeUtils.escapeXml11(res.getUrl()) + "\"/>");
                        out.println("</webLink>");
                        break;
                }
            }
        } catch (Exception e) {
            log.warn("Lessons export error outputting to file, {}", e.toString());
            setErrKey("simplepage.exportcc-fileerr", e.getMessage(), ccConfig.getLocale());
        }
    }
}

