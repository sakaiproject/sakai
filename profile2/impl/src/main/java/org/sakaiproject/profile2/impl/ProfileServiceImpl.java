/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.impl;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLConnection;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.memory.api.SimpleConfiguration;
import org.sakaiproject.profile2.api.MimeTypeByteArray;
import org.sakaiproject.profile2.api.Person;
import org.sakaiproject.profile2.api.ProfileConstants;
import org.sakaiproject.profile2.api.ProfileImage;
import org.sakaiproject.profile2.api.ProfileService;
import org.sakaiproject.profile2.api.ProfileTransferBean;
import org.sakaiproject.profile2.api.SakaiProxy;
import org.sakaiproject.profile2.api.model.ProfileImageOfficial;
import org.sakaiproject.profile2.api.model.ProfileImageUploaded;
import org.sakaiproject.profile2.api.model.SocialNetworkingInfo;
import org.sakaiproject.profile2.api.ProfileDao;
import org.sakaiproject.profile2.util.ProfileUtils;
import org.sakaiproject.profile2.util.Messages;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProfileServiceImpl implements ProfileService, EntityProducer {

    @Autowired private ProfileDao dao;
    @Autowired private EntityManager entityManager;
    @Autowired private MemoryService memoryService;
    @Autowired private SakaiProxy sakaiProxy;
    @Autowired private SessionManager sessionManager;

    private static final String IMAGE_CACHE = "profile2.image.cache";
    private Cache<String, ProfileImage> cache;

    public void init() {

        cache = memoryService.createCache(IMAGE_CACHE, new SimpleConfiguration<>(0));
        entityManager.registerEntityProducer(this, "/profile/");
    }

    public Optional<String> getTool() {
        return Optional.of(ProfileConstants.TOOL_ID);
    }

    @Override
    public ProfileImage getBlankProfileImage() {

        ProfileImage profileImage = new ProfileImage();
        profileImage.setExternalImageUrl(getUnavailableImageURL());
        profileImage.setDefault(true);
        return profileImage;
    }

    @Override
    public ProfileImage getProfileImage(String userUuid, int size) {
        return getProfileImage(userUuid, size, null);
    }

    @Override
    public ProfileImage getOfficialProfileImage(String userUuid, String siteId) {

        ProfileImage profileImage = new ProfileImage();
        profileImage.setDefault(false); //will be overridden if required

        String currentUserId = sakaiProxy.getCurrentUserId();
        String defaultImageUrl = getUnavailableImageURL();

        //check permissions. if not allowed, set default and return
        if (!sakaiProxy.isUserMyWorkspace(siteId)) {
            log.debug("checking if user: " + currentUserId + " has permissions in site: " + siteId);
            if (!sakaiProxy.isUserAllowedInSite(currentUserId, ProfileConstants.ROSTER_VIEW_PHOTO, siteId)) {
                boolean useAvatarInitials = Boolean.valueOf(sakaiProxy.getServerConfigurationParameter("profile2.avatar.initials.enabled", "true"));
                if (useAvatarInitials) {
                    profileImage = this.getProfileAvatarInitials(userUuid);
                    profileImage.setMimeType("image/png");
                } else {
                    profileImage.setExternalImageUrl(defaultImageUrl);
                    profileImage.setDefault(true);
                }
                return profileImage;
            }
        }

        //otherwise get official image
        return getOfficialImage(userUuid, profileImage, defaultImageUrl, StringUtils.equals(userUuid,currentUserId));
    }

    private String getUnavailableImageThumbnailURL() {
       return getUnavailableImageURL(ProfileConstants.UNAVAILABLE_IMAGE_THUMBNAIL);
    }

    @Override
    public ProfileImage getProfileImage(String userUuid, int size, String siteId) {

        ProfileImage image = new ProfileImage();
        boolean allowed = false;
        boolean isSameUser = false;

        image.setDefault(false); //will be overridden if it is actually a default image

        String defaultImageUrl;
        if (ProfileConstants.PROFILE_IMAGE_THUMBNAIL == size) {
            defaultImageUrl = getUnavailableImageThumbnailURL();
        } else {
            defaultImageUrl = getUnavailableImageURL();
        }

        //get current user
        String currentUserUuid = sakaiProxy.getCurrentUserId();
        if (StringUtils.equals(userUuid, currentUserUuid)) {
            isSameUser = true;
        }

        //if no current user we are not logged in (could be from entity provider)
        if (StringUtils.isBlank(currentUserUuid)) {
            // this is where the logic for handling public profile images will go
            //right now we throw a security exception.
            throw new SecurityException("Must be logged in to request a profile image.");
        }

        //check if same user
        if (isSameUser) {
            allowed = true;
        }

        if (sakaiProxy.isSuperUser()) allowed = true;

        //if we have a siteId and it's not a my workspace site, check if the current user has permissions to view the image
        if (StringUtils.isNotBlank(siteId)) {
            if (!sakaiProxy.isUserMyWorkspace(siteId)) {
                log.debug("checking if user: {} has permissions in site: {}", currentUserUuid, siteId);
                allowed = sakaiProxy.isUserAllowedInSite(currentUserUuid, ProfileConstants.ROSTER_VIEW_PHOTO, siteId) ||
                    sakaiProxy.isUserAllowedInSite(currentUserUuid, ProfileConstants.ROSTER_VIEW_PROFILE, siteId);
            }
        }

        //default if still not allowed
        if (!allowed) {
            image.setExternalImageUrl(defaultImageUrl);
            image.setAltText(getAltText(userUuid, isSameUser, false));
            image.setDefault(true);
            return image;
        }

        //check if we have an image for this user type (if enabled)
        //if we have one, return it. otherwise continue to the rest of the checks.
        String userTypeImageUrl = getUserTypeImageUrl(userUuid);
        if (StringUtils.isNotBlank(userTypeImageUrl)) {
            image.setExternalImageUrl(userTypeImageUrl);
            image.setAltText(getAltText(userUuid, isSameUser, true));
            return image;
        }

        //lookup global image setting, this will be used if no preferences were supplied.
        int imageType = sakaiProxy.getProfilePictureType();

        log.debug("image type: {}", imageType);
        log.debug("size requested: {}", size);

        //get the image based on the global type/preference
        switch (imageType) {
            case ProfileConstants.PICTURE_SETTING_UPLOAD:
                MimeTypeByteArray mtba = getUploadedProfileImage(userUuid, size);

                //if no uploaded image, use the default image url
                if (mtba == null || mtba.getBytes() == null) {
                    boolean useAvatarInitials = Boolean.valueOf(sakaiProxy.getServerConfigurationParameter("profile2.avatar.initials.enabled", "true"));
                    if (useAvatarInitials) {
                        image = this.getProfileAvatarInitials(userUuid);
                        image.setMimeType("image/png");
                    } else {
                        image.setExternalImageUrl(defaultImageUrl);
                        image.setDefault(true);
                    }
                } else {
                    image.setUploadedImage(mtba.getBytes());
                    image.setMimeType(mtba.getMimeType());
                }
                image.setAltText(getAltText(userUuid, isSameUser, true));
            break;

            case ProfileConstants.PICTURE_SETTING_OFFICIAL:
                image = getOfficialImage(userUuid,image,defaultImageUrl,isSameUser);
            break;

            default:
                image.setExternalImageUrl(defaultImageUrl);
                image.setAltText(getAltText(userUuid, isSameUser, false));
                image.setDefault(true);
            break;
        }

        return image;
    }

    /**
     * Gets the official image from url, ldap or filesystem, depending on what is specified in props. Filesystem photos
     * are looked up by appending the first letter of a user's eid, then a slash, then the second letter of the eid
     * followed by a slash and finally the eid suffixed by '.jpg'.
     *
     * Like this:
     * /official-photos/a/d/adrian.jpg
     */
    private ProfileImage getOfficialImage(String userUuid, ProfileImage image,String defaultImageUrl, boolean isSameUser) {

        String officialImageSource = sakaiProxy.getOfficialImageSource();

        log.debug("Fetching official image. userUuid: " + userUuid + ", officialImageSource: " + officialImageSource);

        //check source and get appropriate value
        if (StringUtils.equals(officialImageSource, ProfileConstants.OFFICIAL_IMAGE_SETTING_URL)) {
            image.setOfficialImageUrl(getOfficialImageUrl(userUuid));

            //PRFL-790 if URL security is required, get and set bytes and remove url
            if (StringUtils.isNotBlank(image.getOfficialImageUrl())) {
                boolean isFromUrl = false;

                if (getUnavailableImageURL().equals(image.getOfficialImageUrl())) {
                    boolean useAvatarInitials = Boolean.valueOf(sakaiProxy.getServerConfigurationParameter("profile2.avatar.initials.enabled", "true"));
                    if (useAvatarInitials) {
                        image = this.getProfileAvatarInitials(userUuid);
                        image.setMimeType("image/png");
                    } else {
                        image.setExternalImageUrl(defaultImageUrl);
                        image.setDefault(true);
                        isFromUrl = true;
                    }
                } else isFromUrl = true;

                boolean urlSecurityEnabled = Boolean.valueOf(sakaiProxy.getServerConfigurationParameter("profile2.official.image.url.secure", "false"));
                if (urlSecurityEnabled && isFromUrl) {
                    log.debug("URL Security is active");
                    byte[] imageUrlBytes = this.getUrlAsBytes(image.getOfficialImageUrl());
                    image.setUploadedImage(imageUrlBytes);
                    image.setOfficialImageUrl(null);
                }
            }

        } else if (StringUtils.equals(officialImageSource, ProfileConstants.OFFICIAL_IMAGE_SETTING_PROVIDER)) {
            String data = getOfficialImageEncoded(userUuid);
            if (StringUtils.isBlank(data)) {
                boolean useAvatarInitials = Boolean.valueOf(sakaiProxy.getServerConfigurationParameter("profile2.avatar.initials.enabled", "true"));
                if (useAvatarInitials) {
                    image = this.getProfileAvatarInitials(userUuid);
                    image.setMimeType("image/png");
                } else {
                    image.setExternalImageUrl(defaultImageUrl);
                    image.setDefault(true);
                }
            } else {
                image.setOfficialImageEncoded(data);
            }
        } else if (StringUtils.equals(officialImageSource, ProfileConstants.OFFICIAL_IMAGE_SETTING_FILESYSTEM)) {

            //get the path based on the config from sakai.properties, basedir, pattern etc
            String filename = getOfficialImageFileSystemPath(userUuid);

            File file = new File(filename);

            try {
                byte[] data = getBytesFromFile(file);
                if (data != null) {
                    image.setUploadedImage(data);
                } else {
                    boolean useAvatarInitials = Boolean.valueOf(sakaiProxy.getServerConfigurationParameter("profile2.avatar.initials.enabled", "true"));
                    if (useAvatarInitials) {
                        image = this.getProfileAvatarInitials(userUuid);
                        image.setMimeType("image/png");
                    } else {
                        image.setExternalImageUrl(defaultImageUrl);
                        image.setDefault(true);
                    }
                }
            }
            catch (IOException e) {
                log.error("Could not find/read official profile image file: {}. The default profile image will be used instead.", filename);
                image.setExternalImageUrl(defaultImageUrl);
                image.setDefault(true);
            }
        }
        image.setAltText(getAltText(userUuid, isSameUser, true));

        return image;
    }

    @Override
    public ProfileImage getProfileImage(Person person, int size) {
        return getProfileImage(person.getUuid(), size, null);
    }

    @Override
    public ProfileImage getProfileImage(Person person, int size, String siteId) {
        return getProfileImage(person.getUuid(), size, siteId);
    }

    @Override
    public boolean setProfileImage(String userUuid, byte[] imageBytes, String mimeType, String fileName) {

        //check auth and get currentUserUuid
        String currentUserUuid = sakaiProxy.getCurrentUserId();
        if (currentUserUuid == null) {
            throw new SecurityException("You must be logged in to update a user's profile image.");
        }

        //check admin, or the currentUser and given uuid match
        if (!sakaiProxy.isSuperUser() && !StringUtils.equals(currentUserUuid, userUuid)) {
            throw new SecurityException("Not allowed to save.");
        }

        //check image is actually allowed to be changed
        if (!sakaiProxy.isProfilePictureChangeEnabled()) {
            log.warn("Profile image changes are not permitted as per sakai.properties setting 'profile2.picture.change.enabled'.");
            return false;
        }

        /*
         * MAIN PROFILE IMAGE
         */
        //scale image
        byte[] mainImageBytes = ProfileUtils.scaleImage(imageBytes, ProfileConstants.MAX_IMAGE_XY, mimeType);

        //create resource ID
        String mainResourceId = sakaiProxy.getProfileImageResourcePath(userUuid, ProfileConstants.PROFILE_IMAGE_MAIN);

        //save, if error, log and return.
        if (!sakaiProxy.saveFile(mainResourceId, userUuid, fileName, mimeType, mainImageBytes)) {
            log.error("Couldn't add main image to CHS. Aborting.");
            return false;
        }

        /*
         * THUMBNAIL PROFILE IMAGE
         */
        //scale image
        byte[] thumbnailImageBytes = ProfileUtils.scaleImage(imageBytes, ProfileConstants.MAX_THUMBNAIL_IMAGE_XY, mimeType);

        //create resource ID
        String thumbnailResourceId = sakaiProxy.getProfileImageResourcePath(userUuid, ProfileConstants.PROFILE_IMAGE_THUMBNAIL);
        log.debug("Profile.ChangeProfilePicture.onSubmit thumbnailResourceId: " + thumbnailResourceId);

        //save, if error, warn, erase thumbnail reference, and continue (we really only need the main image)
        if (!sakaiProxy.saveFile(thumbnailResourceId, userUuid, fileName, mimeType, thumbnailImageBytes)) {
            log.warn("Couldn't add thumbnail image to CHS. Main image will be used instead.");
            thumbnailResourceId = null;
        }

        /*
         * AVATAR PROFILE IMAGE
         */
        //scale image
        byte[] avatarImageBytes = ProfileUtils.createAvatar(imageBytes, mimeType);

        //create resource ID
        String avatarResourceId = sakaiProxy.getProfileImageResourcePath(userUuid, ProfileConstants.PROFILE_IMAGE_AVATAR);
        log.debug("Profile.ChangeProfilePicture.onSubmit avatarResourceId: " + avatarResourceId);

        //save, if error, warn, erase avatar reference, and continue (we really only need the main image)
        if (!sakaiProxy.saveFile(avatarResourceId, userUuid, fileName, mimeType, avatarImageBytes)) {
            log.warn("Couldn't add avatar image to CHS. Main image will be used instead.");
            avatarResourceId = null;
        }

        /*
         * SAVE IMAGE RESOURCE IDS
         */
        //save
        ProfileImageUploaded profileImage = new ProfileImageUploaded(userUuid, mainResourceId, thumbnailResourceId, avatarResourceId);
        if (dao.saveProfileImage(profileImage)) {
            log.info("Added a new profile image for user: " + userUuid);
            return true;
        }

        return false;

    }

    @Override
    public boolean saveOfficialImageUrl(final String userUuid, final String url) {

        ProfileImageOfficial officialImage = new ProfileImageOfficial(userUuid, url);

        if (dao.saveOfficialImage(officialImage)) {
            log.info("Updated official image record for user: " + userUuid);
            return true;
        }

        return false;
    }

    @Override
    public boolean removeProfileImage(final String userUuid) {

        if (dao.removeProfileImage(userUuid)) {
            log.info("Removed profile image for user: {}", userUuid);
            return true;
        }
        return false;
    }

    @Override
    public boolean profileImageIsDefault(final String userUuid) {
        ProfileImage image = getProfileImage(userUuid, ProfileConstants.PROFILE_IMAGE_MAIN);
        return image.isDefault();
    }


    /**
     * Generate the full URL to the default image (either full or thumbnail)
     * @param imagePath
     * @return
     */
    private String getUnavailableImageURL(String imagePath) {
        StringBuilder path = new StringBuilder();
        path.append(sakaiProxy.getServerUrl());
        path.append(imagePath);
        return path.toString();
    }

    private String getUnavailableImageURL() {
        return getUnavailableImageURL(ProfileConstants.UNAVAILABLE_IMAGE_FULL);
    }

    @Override
    public String getProfileImageEntityUrl(String userId, int size) {

        StringBuilder sb = new StringBuilder();
        sb.append(sakaiProxy.getServerUrl());
        sb.append("/api/users/");
        sb.append(userId);
        sb.append("/profile/image/");
        if (size == ProfileConstants.PROFILE_IMAGE_THUMBNAIL) {
            sb.append("thumb/");
        }
        return sb.toString();
    }

    /**
     * Get the profile image for the given user, allowing fallback if no thumbnail exists.
     *
     * @param userUuid      the uuid of the user we are querying
     * @param size          comes from ProfileConstants, main or thumbnail, also maps to a directory in ContentHosting
     * @return MimeTypeByteArray or null
     *
     * <p>Note: if thumbnail is requested and none exists, the main image will be returned instead. It can be scaled in the markup.</p>
     *
     */
    private MimeTypeByteArray getUploadedProfileImage(String userUuid, int size) {

        MimeTypeByteArray mtba = new MimeTypeByteArray();

        //get record from db
        ProfileImageUploaded profileImage = dao.getProfileImage(userUuid);

        if (profileImage == null) {
            log.debug("ProfileLogic.getUploadedProfileImage() null for userUuid: " + userUuid);
            return null;
        }

        //get main image
        if (size == ProfileConstants.PROFILE_IMAGE_MAIN) {
            mtba = sakaiProxy.getResource(profileImage.getMainResource());
        }

        //or get thumbnail
        if (size == ProfileConstants.PROFILE_IMAGE_THUMBNAIL) {
            mtba = sakaiProxy.getResource(profileImage.getThumbnailResource());
            //PRFL-706, if the file is deleted, catch any possible NPE
            if (mtba == null || mtba.getBytes() == null) {
                mtba = sakaiProxy.getResource(profileImage.getMainResource());
            }
        }

        if (size == ProfileConstants.PROFILE_IMAGE_AVATAR) {
            mtba = sakaiProxy.getResource(profileImage.getAvatarResource());
            //PRFL-706, if the file is deleted, catch any possible NPE
            if (mtba == null || mtba.getBytes() == null) {
                mtba = sakaiProxy.getResource(profileImage.getMainResource());
            }
        }

        return mtba;
    }

    /**
     * Get the URL to a user's official profile image
     * @param userUuid      uuid of user
     *
     * @return url or a default image if none
     */
    private String getOfficialImageUrl(final String userUuid) {

        //get external image record for this user
        ProfileImageOfficial official = dao.getOfficialImage(userUuid);

        //setup default
        String defaultImageUrl = getUnavailableImageURL();

        //if none, return null
        if (official == null) {
            return defaultImageUrl;
        }

        if (StringUtils.isBlank(official.getUrl())) {
            log.info("ProfileLogic.getOfficialImageUrl. No URL for userUuid: " + userUuid + ". Returning default.");
            return defaultImageUrl;
        }

        return official.getUrl();
    }

    /**
     * Get the official image data from the user properties, encoded in BASE64
     * @param userUuid  uuid of user
     * @return base64 encoded data, or null.
     */
    private String getOfficialImageEncoded(final String userUuid) {
        User u = sakaiProxy.getUserById(userUuid);
        return u.getProperties().getProperty(sakaiProxy.getOfficialImageAttribute());
    }

    /**
     * Helper to get the altText to be used for the image
     * @param userUuid
     * @param isOwner
     * @param hasImage
     * @return
     */
    private String getAltText(String userUuid, boolean isOwner, boolean hasImage) {

        //owner and has an image
        if (isOwner && hasImage) {
            return Messages.getString("profile.image.my.alt");
        }

        //owner and doesnt have an image
        if (isOwner && !hasImage) {
            return Messages.getString("profile.image.my.none.alt");
        }

        //not owner so get name
        if (!isOwner) {
            String displayName = sakaiProxy.getUserDisplayName(userUuid);
            return Messages.getString("profile.image.other.alt", new Object[] { displayName });
        }

        return null;
    }

    /**
     * Gets the url to the user type image (PRFL-691)
     *
     *<p>This first checks if the option is enabled (profile2.user.type.image.enabled=true).
     *<p>If so, it attempts to get the value for profile.user.type.image.&lt;usertype&gt; which should be an absolute URL.
     *<p>If there is one, it is returned, if not, return null. Also returns null if userType is undefined.
     * @param userUuid  uuid of the user to get the image for
     * @return url or null.
     */
    private String getUserTypeImageUrl(String userUuid) {

        boolean enabled = Boolean.valueOf(sakaiProxy.getServerConfigurationParameter("profile2.user.type.image.enabled", "false"));

        String imageUrl = null;

        if (enabled) {
            String userType = sakaiProxy.getUserType(userUuid);
            if (StringUtils.isNotBlank(userType)) {
                imageUrl = sakaiProxy.getServerConfigurationParameter("profile.user.type.image."+userType, null);
            }
        }
        return imageUrl;
    }

    /**
     * Read a file to a byte array.
     *
     * @param file
     * @return byte[] if file ok, or null if its too big
     * @throws IOException
     */
    private byte[] getBytesFromFile(File file) throws IOException {

        // Get the size of the file
        long length = file.length();

        if (length > (ProfileConstants.MAX_IMAGE_UPLOAD_SIZE * FileUtils.ONE_MB)) {
            log.error("File too large: " + file.getCanonicalPath());
            return null;
        }

        // return file contents
        return FileUtils.readFileToByteArray(file);
   }

    /**
     * Helper to get the path to the official image on the filesystem. This could be in one of several patterns.
     * @param userUuid
     * @return
     */
    private String getOfficialImageFileSystemPath(String userUuid) {

        //get basepath, common to all
        String basepath = sakaiProxy.getOfficialImagesDirectory();

        //get the pattern
        String pattern = sakaiProxy.getOfficialImagesFileSystemPattern();

        //get user, common for all
        User user = sakaiProxy.getUserById(userUuid);
        String userEid = user.getEid();

        String filename = null;

        //create the path based on the basedir and pattern
        if (StringUtils.equals(pattern, "ALL_IN_ONE")) {
            filename =  basepath + File.separator + userEid + ".jpg";
        }
        else if (StringUtils.equals(pattern, "ONE_DEEP")) {
            String firstLetter = userEid.substring(0,1);
            filename = basepath + File.separator + firstLetter + File.separator + userEid + ".jpg";
        }
        //insert more patterns here as required. Dont forget to update SakaiProxy and confluence with details.
        else {
            //TWO_DEEP is default
            String firstLetter = userEid.substring(0,1);
            String secondLetter = userEid.substring(1,2);
            filename = basepath + File.separator + firstLetter + File.separator + secondLetter + File.separator + userEid + ".jpg";
        }

        if (log.isDebugEnabled()) {
            log.debug("Path to official image on filesystem is: " + filename);
        }

        return filename;
    }

    /**
     * Get a URL resource as a byte[]
     * @param url URL to fetch
     * @return
     */
    private byte[] getUrlAsBytes(String url) {
        byte[] data = null;
        try {
            URL u = new URL(url);
            URLConnection uc = u.openConnection();
            uc.setReadTimeout(5000); //5 sec timeout
            InputStream inputStream = uc.getInputStream();

            data = IOUtils.toByteArray(inputStream);

        } catch (Exception e) {
            log.error("Failed to retrieve url bytes: " + e.getClass() + ": " + e.getMessage());
        }
        return data;
    }

    @Override
    public ProfileImage getProfileAvatarInitials(String userUuid) {

        ProfileImage image = null;
        if (cache.containsKey(userUuid)) {
            image = cache.get(userUuid);
            if (image == null) {
                cache.remove(userUuid);
            }

        }
        if (image == null) {
            image = new ProfileImage();
            BufferedImage bufferedImage = new BufferedImage(ProfileConstants.PROFILE_AVATAR_WIDTH, ProfileConstants.PROFILE_AVATAR_HEIGHT, BufferedImage.TYPE_INT_ARGB);

            String displayName = sakaiProxy.getUserDisplayName(userUuid);
            String[] names = displayName.trim().split("\\s+");
            String initials = "";
            int fontSize;
            int profileInitialsSize = Integer.parseInt(sakaiProxy.getServerConfigurationParameter("profile2.avatar.initials.size", "2"));
            switch(profileInitialsSize) {
                case 1:
                    initials = Character.toString(names[0].charAt(0));
                    fontSize = Integer.parseInt(sakaiProxy.getServerConfigurationParameter("profile2.avatar.initials.font.size", ProfileConstants.DFLT_PROFILE_AVATAR_FONT_SIZE_1_CHAR));
                    break;
                case 2:
                default:
                    for (int i=0; i < names.length;i++) {
                        if (i > 1) break;
                        initials += Character.toString(names[i].charAt(0));
                    }
                    fontSize = Integer.parseInt(sakaiProxy.getServerConfigurationParameter("profile2.avatar.initials.font.size", ProfileConstants.DFLT_PROFILE_AVATAR_FONT_SIZE_2_CHAR));
                    break;
            }
            initials = initials.toUpperCase();

            Graphics2D background = bufferedImage.createGraphics();
            background.setPaint(Color.decode(this.getAvatarInitialsColor(displayName)));
            background.fillRect(0, 0, ProfileConstants.PROFILE_AVATAR_WIDTH, ProfileConstants.PROFILE_AVATAR_HEIGHT);

            String fontFamily = sakaiProxy.getServerConfigurationParameter("profile2.avatar.initials.font", ProfileConstants.DFLT_PROFILE_AVATAR_FONT_FAMILY);
            Graphics2D initialsg2d = bufferedImage.createGraphics();
            initialsg2d.setPaint(Color.WHITE);
            initialsg2d.setFont(new Font(fontFamily, Font.PLAIN, fontSize));
            FontMetrics fm = initialsg2d.getFontMetrics();
            int x = (ProfileConstants.PROFILE_AVATAR_WIDTH/2) - fm.stringWidth(initials) / 2;
            int y = (ProfileConstants.PROFILE_AVATAR_HEIGHT - fm.getHeight()) / 2 + fm.getAscent();
            initialsg2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            initialsg2d.drawString(initials, x, y);
            initialsg2d.dispose();

            byte[] bytes = null;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(bufferedImage, "png", baos);
                bytes = baos.toByteArray();
            } catch (IOException ex) {
                log.error("Cannot generate profile avatar for the user {}", userUuid);
            }

            if (bytes != null) {
                image.setUploadedImage(bytes);
            } else {
                image.setExternalImageUrl(getUnavailableImageURL());
                image.setDefault(true);
            }
            image.setInitials(true);
            cache.put(userUuid, image);
        }
        return image;
    }

    private String getAvatarInitialsColor(String displayName) {
        int rand = 0;
        for (int i=0;i<displayName.length();i++) {
            rand += displayName.charAt(i);
        }
        String[] profileAvatarColors = sakaiProxy.getServerConfigurationParameter("profile2.avatar.initials.colors", ProfileConstants.DFLT_PROFILE_AVATAR_COLORS).split(",");
        int colorIndex = (int) (Math.floor(rand % profileAvatarColors.length));
        return profileAvatarColors[colorIndex];
    }

    @Override
    public ProfileTransferBean getUserProfile(String userId) {
        return getUserProfile(userId, null);
    }

    @Override
    public ProfileTransferBean getUserProfile(String userId, String siteId) {

        String currentUserId = sessionManager.getCurrentSessionUserId();

        if (currentUserId == null) {
            throw new SecurityException("Must be logged in to get a UserProfile.");
        }

        //get User
        User u = sakaiProxy.getUserById(userId);
        if (u == null) {
            log.error("User {} does not exist.", userId);
            return null;
        }

        ProfileTransferBean p = new ProfileTransferBean();
        p.id = userId;
        p.eid = u.getEid();
        p.firstName = u.getFirstName();
        p.lastName = u.getLastName();
        p.displayName = u.getDisplayName();
        p.imageUrl = getProfileImageEntityUrl(userId, ProfileConstants.PROFILE_IMAGE_MAIN);
        p.imageThumbUrl = getProfileImageEntityUrl(userId, ProfileConstants.PROFILE_IMAGE_THUMBNAIL);
        p.creatorDisplayName = u.getCreatedBy().getDisplayName();
        p.hasPronunciationRecording = sakaiProxy.resourceExists(getUserNamePronunciationResourceId(userId));
        p.modifierDisplayName = u.getModifiedBy().getDisplayName();
        p.type = u.getType();

        p.canUpdatePicture = (StringUtils.equals(currentUserId, userId) && sakaiProxy.isProfilePictureChangeEnabled()) || sakaiProxy.isSuperUser();
        p.canEdit = StringUtils.equals(currentUserId, userId) || sakaiProxy.isSuperUser();
        p.canEditNameAndEmail = sakaiProxy.isSuperUser();

        Optional<SakaiPerson> sakaiPerson = sakaiProxy.getSakaiPerson(userId);
        sakaiPerson.ifPresent(sp -> {
            p.nickname = sp.getNickname();
            p.phoneticPronunciation = sp.getPhoneticPronunciation();
            p.pronouns = sp.getPronouns();
            p.mobile = sp.getMobile();
        });

        dao.getSocialNetworkingInfo(userId).ifPresent(socialInfo -> {
            p.facebookUrl = socialInfo.getFacebookUrl();
            p.instagramUrl = socialInfo.getInstagramUrl();
            p.linkedinUrl = socialInfo.getLinkedinUrl();
        });

        if (StringUtils.equals(userId, currentUserId) || sakaiProxy.isSuperUser()) {
            p.email = u.getEmail();
            return p;
        }

        // Add email if allowed, remove contact info if not
        if (siteId != null && sakaiProxy.isUserAllowedInSite(currentUserId, ProfileConstants.ROSTER_VIEW_EMAIL, siteId)) {
            p.email = u.getEmail();
        }

        return p;
    }

    @Override
    public boolean hasPronunciationRecording(String userId) {
        return sakaiProxy.resourceExists(getUserNamePronunciationResourceId(userId));
    }

    @Override
    @Transactional
    public boolean saveUserProfile(ProfileTransferBean profileBean) {

        Optional<SakaiPerson> sakaiPerson = sakaiProxy.getSakaiPerson(profileBean.id);
        sakaiPerson.ifPresent(sp -> {
            sp.setNickname(profileBean.nickname);
            sp.setPronouns(profileBean.pronouns);
            sp.setPhoneticPronunciation(profileBean.phoneticPronunciation);
            sp.setMobile(profileBean.mobile);
        });

        SocialNetworkingInfo socialInfo
          = dao.getSocialNetworkingInfo(profileBean.id).orElseGet(() -> new SocialNetworkingInfo(profileBean.id));

        socialInfo.setFacebookUrl(profileBean.facebookUrl);
        socialInfo.setInstagramUrl(profileBean.instagramUrl);
        socialInfo.setLinkedinUrl(profileBean.linkedinUrl);

        dao.saveSocialNetworkingInfo(socialInfo);

        if (profileBean.audioBase64 != null) {
            try {
              String path = getUserNamePronunciationResourceId(profileBean.id);
              byte[] bytes = Base64.getDecoder().decode(profileBean.audioBase64);
              sakaiProxy.removeResource(path);
              sakaiProxy.saveFile(path, profileBean.id, profileBean.id + ".ogg", "audio/ogg; codecs=opus", bytes);
            } catch (Exception e) {
                log.error("Could not save name pronunciation recording for user {}: {}", profileBean.id, e.toString());
            }
        }

        return sakaiPerson.map(sp -> {
            boolean updated = sakaiProxy.updateSakaiPerson(sp);
            if (updated) sendProfileChangeEmailNotification(sp.getAgentUuid());
            return updated;
        }).orElse(false);
    }

    public boolean removePronunciationRecording(String userId) {

        String path = getUserNamePronunciationResourceId(userId);
        sakaiProxy.removeResource(path);
        ProfileTransferBean userProfile = getUserProfile(userId);
        userProfile.nameRecordingUrl = null;
        saveUserProfile(userProfile);
        return true;
    }

    /**
     * Sends an email notification when a user changes their profile, if enabled.
     *
     * @param userUuid the uuid of the user who changed their profile
     */
    private void sendProfileChangeEmailNotification(final String userUuid) {

        //check if option is enabled
        boolean enabled = Boolean.valueOf(sakaiProxy.getServerConfigurationParameter("profile2.profile.change.email.enabled", "false"));
        if (!enabled) {
            return;
        }

        //get the user to send to. THis will be translated into an internal ID. Since SakaiProxy.sendEmail takes a userId as a param
        //it was easier to require an eid here (and thus the person needs to have an account) rather than create a new method that takes an email address.
        String eidTo = sakaiProxy.getServerConfigurationParameter("profile2.profile.change.email.eid", null);
        if (StringUtils.isBlank(eidTo)) {
            log.error("Profile change email notification is enabled but no user eid to send it to is set. Please set 'profile2.profile.change.email.eid' in sakai.properties");
            return;
        }

        //get internal id for this user
        String userUuidTo = sakaiProxy.getUserIdForEid(eidTo);
        if (StringUtils.isBlank(userUuidTo)) {
            log.error("Profile change email notification is setup with an invalid eid. Please adjust 'profile2.profile.change.email.eid' in sakai.properties");
            return;
        }

        return;
    }

    private String getUserNamePronunciationResourceId(String uuid) {

        String slash = Entity.SEPARATOR;
        StringBuilder path = new StringBuilder();
        path.append(Entity.SEPARATOR);
        path.append("private");
        path.append(Entity.SEPARATOR);
        path.append("namePronunciation");
        path.append(Entity.SEPARATOR);
        path.append(uuid);
        path.append(".ogg");

        return path.toString();
    }

    @Override
    public MimeTypeByteArray getUserNamePronunciation(String uuid) {
        String resourceId = getUserNamePronunciationResourceId(uuid);
        return sakaiProxy.getResource(resourceId);
    }

    @Override
    public String getProfileImageURL(String userId, boolean thumbnail) {

        if (userId == null) {
            return thumbnail ? ProfileConstants.UNAVAILABLE_IMAGE_THUMBNAIL : ProfileConstants.UNAVAILABLE_IMAGE_FULL;
        }

        return "/api/users/" + userId + "/profile/image" + (thumbnail ? "/thumb" : "");
    }
}
