/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.user.util;

// Sakai 11 introduced a new way of storing users' favorite sites, which
// simplifies the preferences data model somewhat.
//
// Prior to Sakai 11, the situation with preferences was:
//
//   * The user's sakai:portal:sitenav.order preference contains a list of Site
//     IDs that may or may not be favorites. This is initially empty.
//
//   * When the user selects an orders some favorite sites, those are added to
//     the start of the sakai:portal:sitenav.order list.
//
//   * If the user hides one or more sites (using the preferences tool), all
//     non-hidden sites are added to the sakai:portal:sitenav.order list, even
//     though they haven't been explicitly marked as favorites.
//
//   * The sakai:portal:sitenav.tabs property is a number that controls how many
//     sites from sakai:portal:sitenav.order should be shown
//
// So, to determine the list of favorite sites for a user, we need to:
//
//   * Take the list of sites from sakai:portal:sitenav.order
//
//   * If sakai:portal:sitenav.tabs is set, only keep that many sites from the
//     list. Otherwise, take all of them.
//
//   * If we have fewer than 5 sites, arbitrarily select more to make up the
//     numbers.
//
// From Sakai 11 onwards, the situation is now:
//
//    * The sakai:portal:sitenav.order preference contains a list of site IDs.
//      Those are the user's favorites in order.
//
// This class sets each user's sakai:portal:sitenav.order property to the list
// of sites that they would have seen as favorites (in the bar at the top of
// each page) prior to Sakai 11.
//
// This conversion is intended to be run as a part of the upgrade process to
// Sakai 11.  If it gets interrupted while running, it can safely be run again.

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.sakaiproject.user.api.PreferencesService;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

@Slf4j
class ConvertUserFavoriteSitesSakai11 {

    // The maximum number of favorites we'll give to a user who needs some
    static int FAVORITES_TO_GRANT = 5;

    static int UPDATES_PER_TRANSACTION = 200;

    private static void info(String msg) {
        log.info(msg);
    }

    private static void debug(String msg) {
        if (log.isDebugEnabled()) {
            log.debug("*** DEBUG: " + msg);
        }
    }

    public static void main(String args[]) {
        String tomcatDir = System.getProperty("tomcat.dir");
        String dbPropertiesPath = System.getProperty("db.properties");

        if (tomcatDir == null && dbPropertiesPath == null) {
            info("You must either set the tomcat.dir system property, or the db.properties system property.\n");
            showUsage();

            System.exit(1);
        }

        try {
            DBConfig config = new DBConfig(dbPropertiesPath, tomcatDir);

            Connection db = null;
            try {
                db = DriverManager.getConnection(config.getUrl(),
                        config.getUsername(),
                        config.getPassword());

                migrateFavoriteSites(db);
            } finally  {
                if (db != null) {
                    db.close();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void migrateFavoriteSites(Connection db) throws
        SQLException {
        PreparedStatement countPreferences = null;
        PreparedStatement usersSiteMemberships = null;
        PreparedStatement selectPreferences = null;
        PreparedStatement updatePreference = null;
        ResultSet rs = null;

        db.setAutoCommit(false);

        try {
            countPreferences = db.prepareStatement("select count(1) from SAKAI_PREFERENCES");

            rs = countPreferences.executeQuery();
            int totalPreferences = 0;
            if (rs.next()) {
                totalPreferences = rs.getInt(1);
            }

            info("Preferences to migrate: " + totalPreferences);

            ProgressCounter counter = new ProgressCounter(totalPreferences);

            // There's a lot here!  What we're doing:
            //
            //  * Find any user who has a preferences entry
            //
            //  * Join them with their site memberships, where those sites:
            //
            //      - are published (or where the user has access to the unpublished site)
            //      - are not a workspace
            //      - are not a special site
            //      - haven't been deleted
            //
            //  * Sites that don't meet this criteria will yield NULLs, and
            //    that's OK--this ensures we always get at least one row for a
            //    given preference record, even if that user isn't in any sites.
            //    We'll drop them out as we notice them.
            usersSiteMemberships = db.prepareStatement("select p.preferences_id as userId, ss.site_id as siteId" +
                                                       " from SAKAI_PREFERENCES p" +
                                                       " left outer join SAKAI_SITE_USER ssu on ssu.user_id = p.preferences_id" +
                                                       " left outer join SAKAI_SITE ss on ss.site_id = ssu.site_id" +
                                                       "    AND ss.is_user = '0'" +
                                                       "    AND ss.is_special = '0'" +
                                                       "    AND ssu.permission <= ss.published" +
                                                       "    AND ss.is_softly_deleted = '0'" +
                                                       " order by p.preferences_id, ss.createdon desc");

            selectPreferences = db.prepareStatement("select xml from SAKAI_PREFERENCES where preferences_id = ?");

            updatePreference = db.prepareStatement("update SAKAI_PREFERENCES set xml = ? where preferences_id = ?");

            PreferenceMigrator migrator = new PreferenceMigrator();

            rs = usersSiteMemberships.executeQuery();

            Iterator<UserSitesIterator.Entry> it = new UserSitesIterator(rs);

            int pendingRecords = 0;
            while (it.hasNext()) {
                counter.tick();

                UserSitesIterator.Entry entry = it.next();

                selectPreferences.setString(1, entry.userId);
                ResultSet preferencesResultSet = selectPreferences.executeQuery();
                if (preferencesResultSet.next()) {
                    String xml = preferencesResultSet.getString("xml");
                    try {
                        String migratedXml = migrator.migratePreferences(xml, entry.sites);

                        updatePreference.setString(1, migratedXml);
                        updatePreference.setString(2, entry.userId);

                        updatePreference.executeUpdate();
                        pendingRecords++;

                        if ((pendingRecords % UPDATES_PER_TRANSACTION) == 0) {
                            db.commit();
                        }
                    } catch (PreferenceMigrator.PreferenceMigrateFailedException e) {
                        info("Failed to migrate preferences for user: " + entry.userId + ". Skipped!");
                        info("Migration error was: " + e);
                        info("\n");
                        log.error(e.getMessage(), e);
                    }
                } else {
                    info("Couldn't fetch preferences for user: " + entry.userId + ".  Skipped!");
                }
            }

            if (pendingRecords > 0) {
                db.commit();
            }
        } finally {
            if (rs != null) { rs.close(); }
            if (usersSiteMemberships != null) { usersSiteMemberships.close(); }
            if (selectPreferences != null) { selectPreferences.close(); }
            if (updatePreference != null) { updatePreference.close(); }
        }

        info("Migration complete!");
    }

    private static void showUsage() {
        info("Usage:\n");
        info("  cd /path/to/my/tomcat/directory");

        info("\nThen, for Unix:\n");
        info("  java -cp \"lib/*\" -Dtomcat.dir=\"$PWD\" org.sakaiproject.user.util.ConvertUserFavoriteSitesSakai11");

        info("\nOr Windows:\n");
        info("  java -cp \"lib\\*\" -Dtomcat.dir=%cd% org.sakaiproject.user.util.ConvertUserFavoriteSitesSakai11\n");

        info("\nIf the properties file containing your database connection details is stored in a non-standard location, you can explicitly select it with:\n");
        info("  java -cp \"lib\\*\" -Ddb.properties=my_database.properties org.sakaiproject.user.util.ConvertUserFavoriteSitesSakai11\n");

    }


    //
    // Show a progress counter and some performance numbers
    private static class ProgressCounter {

        // Show a status message every REPORT_FREQUENCY_MS milliseconds
        private static long REPORT_FREQUENCY_MS = 10000;

        private long estimatedTotalRecordCount = 0;
        private long recordCount = 0;
        private long startTime;
        private long timeOfLastReport = 0;

        public ProgressCounter(long estimatedTotalRecordCount) {
            this.estimatedTotalRecordCount = estimatedTotalRecordCount;
            this.startTime = System.currentTimeMillis();
        }

        public void tick() {
            recordCount++;

            if (recordCount > estimatedTotalRecordCount) {
                // lie :)
                recordCount = estimatedTotalRecordCount;
            }

            long now = System.currentTimeMillis();
            long msSinceLastReport = (now - timeOfLastReport);

            if (msSinceLastReport >= REPORT_FREQUENCY_MS || recordCount == estimatedTotalRecordCount) {
                timeOfLastReport = now;
                info("\nUp to record number " + recordCount + " of " + estimatedTotalRecordCount);

                long elapsed = (timeOfLastReport - startTime);

                if (elapsed > 0) {
                    float recordsPerSecond = (recordCount / (float)elapsed) * 1000;

                    info(String.format("Average processing rate (records/second): %.2f", + recordsPerSecond));
                    long recordsRemaining = (estimatedTotalRecordCount - recordCount);
                    long msRemaining = (long)((recordsRemaining / recordsPerSecond) * 1000);
                    info("Estimated finish time: " + new Date(System.currentTimeMillis() + msRemaining));
                }
            }
        }
    }


    //
    // Given a result set of rows mapping users to their sites, provide an
    // iterator that groups the results by user.  Result set must be sorted by
    // userId already.
    //
    private static class UserSitesIterator implements Iterator<UserSitesIterator.Entry> {
        private ResultSet rs;
        private boolean hitEOF = false;

        public UserSitesIterator(ResultSet rs) {
            this.rs = rs;
            bufferRow();
        }

        public boolean hasNext() {
            return !hitEOF;
        }

        public Entry next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            try {
                String userId = rs.getString("userId");
                Entry result = new Entry(userId);

                do {
                    if (rs.getString("userId").equals(userId)) {
                        if (rs.getString("siteId") != null) {
                            result.sites.add(rs.getString("siteId"));
                        }
                    } else {
                        break;
                    }
                } while (bufferRow());

                return result;

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        private boolean bufferRow() {
            try {
                if (rs.next()) {
                    return true;
                } else {
                    hitEOF = true;
                    return false;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public class Entry {
            public String userId;
            public List<String> sites;

            public Entry(String userId) {
                this.userId = userId;
                sites = new ArrayList<String>();
            }

            public String toString() {
                return userId + " - " + sites;
            }
        }
    }


    //
    // Find the user's database connection settings
    //
    private static class DBConfig {
        private String username;
        private String password;
        private String url;

        public DBConfig(String propertiesFile, String tomcatDir) {
            if (propertiesFile != null) {
                loadFromProperties(propertiesFile);
            } else {
                for (String possibleProperties : findPropertiesFiles(tomcatDir)) {
                    loadFromProperties(possibleProperties);
                }
            }

            if (username == null || password == null || url == null) {
                throw new RuntimeException("Could not locate your database connection settings!");
            }
        }

        public String getUrl() { return url; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }


        private void loadFromProperties(String file) {
            Properties properties = new Properties();

            try {
                FileInputStream fh = new FileInputStream(file);
                properties.load(fh);
                fh.close();
            } catch (IOException e) {
                ConvertUserFavoriteSitesSakai11.info("Failed to read properties from: " + file);
            }

            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String prop = (String) entry.getKey();
                String value = (String) entry.getValue();

                if ("driverClassName@javax.sql.BaseDataSource".equals(prop)) {
                    try {
                        Class.forName(value);
                    } catch (ClassNotFoundException e) {
                        ConvertUserFavoriteSitesSakai11.info("*** Failed to load database driver!");
                        throw new RuntimeException(e);
                    }
                } else if ("url@javax.sql.BaseDataSource".equals(prop)) {
                    this.url = value;
                } else if ("username@javax.sql.BaseDataSource".equals(prop)) {
                    this.username = value;
                } else if ("password@javax.sql.BaseDataSource".equals(prop)) {
                    this.password = value;
                }
            }
        }

        private List<String> findPropertiesFiles(String tomcatDir) {
            List<String> result = new ArrayList();

            for (String path : new String[] { "sakai" + File.separator + "sakai.properties",
                                              "sakai" + File.separator + "local.properties",
                                              "sakai" + File.separator + "instance.properties" }) {
                if (new File(tomcatDir + File.separator + path).exists()) {
                    result.add(path);
                }
            }

            return result;
        }
    }


    //
    // Do the actual business of updating a preferences XML document for the new format
    //
    private static class PreferenceMigrator {

        public class PreferenceMigrateFailedException extends Exception {

            public PreferenceMigrateFailedException(String message, Throwable cause) {
                super(message, cause);

            }
        }

        private DocumentBuilder documentBuilder;
        private XPath xpath;
        String prefsPrefix = "/preferences/prefs[@key='" + PreferencesService.SITENAV_PREFS_KEY + "']";

        public PreferenceMigrator() {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

            try {
                documentBuilder = dbFactory.newDocumentBuilder();
                xpath = XPathFactory.newInstance().newXPath();
            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            }
        }

        public String migratePreferences(String xml, List<String> userSites) throws PreferenceMigrateFailedException {
            try {
                InputSource inputSource = new InputSource(new StringReader(xml));
                Document doc = documentBuilder.parse(inputSource);

                List<String> orderSiteIds = parsePreferenceList(prefsPrefix + "/properties/property[@name='order']", doc);
                List<String> hiddenSiteIds = parsePreferenceList(prefsPrefix + "/properties/property[@name='exclude']", doc);
                List<String> tabsValue = parsePreferenceList(prefsPrefix + "/properties/property[@name='tabs']", doc);

                // tabCount is the number of entries in orderSiteIds that are "real" favorites
                int tabCount = tabsValue.isEmpty() ? orderSiteIds.size() : Integer.valueOf(tabsValue.get(0));

                List<String> finalUserFavorites = new ArrayList<String>(5);

                // Start with any favorite sites the user already has
                finalUserFavorites.addAll(orderSiteIds.subList(0, Math.min(tabCount, orderSiteIds.size())));

                // If that didn't yield any favorites (because they've never set
                // any), give them up to FAVORITES_TO_GRANT automatically.
                if (tabsValue.isEmpty() && finalUserFavorites.isEmpty()) {
                    for (String siteId : userSites) {
                        if (finalUserFavorites.size() == FAVORITES_TO_GRANT) {
                            break;
                        }

                        if (!hiddenSiteIds.contains(siteId) && !finalUserFavorites.contains(siteId)) {
                            finalUserFavorites.add(siteId);
                        }
                    }
                }

                // Finally, patch the new favorites into our preferences XML
                replaceOrderList(doc, finalUserFavorites);

                debug("===========================================================================");
                debug("Previously:");
                debug(xml);
                debug("===========================================================================");
                debug("Migrated:");
                debug(toXML(doc));
                debug("===========================================================================\n");

                return toXML(doc);
            } catch (Exception e) {
                throw new PreferenceMigrateFailedException(e.getMessage(), e);
            }
        }

        private void replaceOrderList(Document doc, List<String> userFavorites) throws Exception {

            NodeList nodesToDelete = (NodeList)xpath.evaluate(prefsPrefix + "/properties/property[@name='order' or @name='tabs']", doc, XPathConstants.NODESET);

            // delete any old order or tabs nodes
            for (int i = 0; i < nodesToDelete.getLength(); i++) {
                Node victim = nodesToDelete.item(i);
                Node parent = nodesToDelete.item(i).getParentNode();

                parent.removeChild(victim);
            }

            // If the user has no favorites, we're done.
            if (userFavorites.isEmpty()) {
                return;
            }

            // Make sure we actually have the properties we need
            Node existingSiteNav = (Node) xpath.evaluate(prefsPrefix, doc, XPathConstants.NODE);

            if (existingSiteNav == null) {
                // Add the top-level sitenav property
                Element siteNav = doc.createElement("prefs");
                siteNav.setAttribute("key", PreferencesService.SITENAV_PREFS_KEY);
                doc.getDocumentElement().appendChild(siteNav);

                existingSiteNav = (Node) xpath.evaluate(prefsPrefix, doc, XPathConstants.NODE);
            }

            // siteNav should have a "properties" element under it
            Node existingSiteNavProperties = (Node) xpath.evaluate(prefsPrefix + "/properties", doc, XPathConstants.NODE);

            if (existingSiteNavProperties == null) {
                // Add the top-level sitenav property
                Element siteNav = doc.createElement("properties");
                existingSiteNav.appendChild(siteNav);

                existingSiteNavProperties = (Node) xpath.evaluate(prefsPrefix + "/properties", doc, XPathConstants.NODE);
            }

            // Create new ones
            for (String siteId : userFavorites) {
                Element newProperty = doc.createElement("property");
                newProperty.setAttribute("enc", "BASE64");
                newProperty.setAttribute("list", "list");
                newProperty.setAttribute("name", "order");
                newProperty.setAttribute("value", encodeBase64(siteId));

                existingSiteNavProperties.appendChild(newProperty);
            }
        }

        private List<String> parsePreferenceList(String xpathQuery, Document doc) throws Exception {
            List<String> result = new ArrayList<String>();
            NodeList orderNodes = (NodeList)xpath.evaluate(xpathQuery, doc, XPathConstants.NODESET);

            for (int i = 0; i < orderNodes.getLength(); i++) {
                Node orderNode = orderNodes.item(i);
                String encodedEntry = orderNode.getAttributes().getNamedItem("value").getTextContent();

                result.add(decodeBase64(encodedEntry));
            }

            return result;
        }

        private String decodeBase64(String s) throws Exception {
            return new String(DatatypeConverter.parseBase64Binary(s),
                              "UTF-8");
        }

        private String encodeBase64(String s) throws Exception {
            return DatatypeConverter.printBase64Binary(s.getBytes("UTF-8"));
        }

        private String toXML(Document doc) {
            StringWriter result = new StringWriter();

            DOMImplementation impl = documentBuilder.getDOMImplementation();
            DOMImplementationLS feature = (DOMImplementationLS) impl.getFeature("LS", "3.0");
            LSSerializer serializer = feature.createLSSerializer();
            LSOutput lsoutput = feature.createLSOutput();
            lsoutput.setCharacterStream(result);
            lsoutput.setEncoding("UTF-8");
            serializer.write(doc, lsoutput);

            result.flush();

            return result.toString();
        }
    }
}
