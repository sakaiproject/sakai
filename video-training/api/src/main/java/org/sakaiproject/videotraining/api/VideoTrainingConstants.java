/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.videotraining.api;

import java.util.Set;
import java.util.regex.Pattern;

import org.sakaiproject.entity.api.Entity;

public final class VideoTrainingConstants {

    // APPLICATION & GENERAL IDENTIFIERS
    public static final String APPLICATION_ID = "sakai:video-training";
    public static final String REFERENCE_ROOT = Entity.SEPARATOR + "video-training";
    public static final String UTF8 = "UTF-8";

    // DEFAULT VALUES
    public static final String DEFAULT_APPLICATION_NAME = "Sakai Video Training";
    public static final String DEFAULT_SERVICE_NAME = "Video Training";

    // Folders & Roots
    public static final String DEFAULT_BASE_FOLDER = "Video Training";
    public static final String DEFAULT_GLOBAL_ROOT = "/public/video-training/";
    public static final String DEFAULT_GLOBAL_ROOT_BASE_FOLDER = "Video Training";

    // Server Configuration Defaults
    public static final boolean DEFAULT_FOLDER_HIDDEN_WITH_ACCESS = true;
    public static final boolean DEFAULT_HLS_ENABLED = false;
    public static final boolean DEFAULT_MODERATION_ENABLED = false;
    public static final String DEFAULT_HLS_FFMPEG = "ffmpeg";
    public static final String DEFAULT_MAX_NATIVE_UPLOAD_SIZE = "512";
    public static final String DEFAULT_OAUTH_ENCRYPTION_KEY = null;
    public static final String DEFAULT_PROVIDER_YOUTUBE_API_KEY = "";
    public static final String DEFAULT_PROVIDER_YOUTUBE_CATEGORY_ID = "27";
    public static final String DEFAULT_SMTP_EMAIL_SERVICE = null;

    // MEDIA, FILES & CODECS
    public static final String AUDIO_CODEC_AAC = "aac";
    public static final String VIDEO_CODEC_LIBX264 = "libx264";
    public static final String DEFAULT_MP4_CONTENT_TYPE = "video/mp4";
    public static final String HLS_CONTAINER_FORMAT = "hls";
    public static final String IMAGE_CONTAINER_FORMAT = "image2";
    public static final String MASTER_PLAYLIST_FILENAME = "master.m3u8";
    public static final String THUMBNAIL_FILENAME = "thumbnail.jpg";
    public static final String DEFAULT_THUMBNAIL_TIMESTAMP = "00:00:01.000";

    // PAGINATION & UPLOADS
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final long BYTES_PER_MB = 1024L * 1024L;
    public static final long DEFAULT_MAX_NATIVE_UPLOAD_MB = 512L;
    public static final Set<String> ALLOWED_NATIVE_VIDEO_EXTENSIONS = Set.of("mp4", "webm", "ogg", "mov", "m4v", "mkv");

    // CACHE
    public static final String COUNT_CACHE_NAME = "org.sakaiproject.videotraining.cache.list.count";
    public static final String FIRST_PAGE_CACHE_NAME = "org.sakaiproject.videotraining.cache.list.firstPageIds";

    // PATHS, MODES & VIEWS
    // Paths
    public static final String FAVORITES_PATH = "/favorites";
    public static final String MANAGEABLE_LIST_PATH = "/videos-manageable";
    public static final String VIEWABLE_LIST_PATH = "/videos-viewable";
    public static final String WATCH_LATER_PATH = "/watch-later";

    // Access & Source Modes
    public static final String ACCESS_MODE_MANAGEABLE = "manageable";
    public static final String ACCESS_MODE_VIEWABLE = "viewable";
    public static final String SOURCE_MODE_EXTERNAL = "external";
    public static final String SOURCE_MODE_RESOURCES = "resources";
    public static final String SOURCE_MODE_UPLOAD = "upload";

    // Views & Sorting
    public static final String VIEW_MODE_CARDS = "cards";
    public static final String VIEW_MODE_TABLE = "table";
    public static final String VIEW_MODE_SESSION_PREFIX = "video-training.list.view-mode.";
    public static final String DEFAULT_SORT_DIRECTION = "desc";
    public static final String DEFAULT_SORT_FIELD = "modifiedOn";

    // PERMISSIONS
    public static final String PERMISSION_PREFIX = "video.training";
    public static final String PERMISSION_ANALYTICS = "video.training.analytics";
    public static final String PERMISSION_CATEGORIES_MANAGE = "video.training.categories.manage";
    public static final String PERMISSION_MANAGE = "video.training.manage";
    public static final String PERMISSION_MANAGE_ALL = "video.training.manage.all";
    public static final String PERMISSION_APPROVE_PUBLISH = "video.training.approve";
    public static final String PERMISSION_GLOBAL = "video.training.global";
    public static final String PERMISSION_VIEW = "video.training.view";

    // CRYPTOGRAPHY & SECURITY
    public static final String ALGORITHM = "AES/GCM/NoPadding";
    public static final int IV_LENGTH = 12;
    public static final int TAG_LENGTH_BIT = 128;

    // REGEX PATTERNS
    public static final Pattern IFRAME_PATTERN = Pattern.compile("(?is)<iframe[^>]*\\bsrc=[\"']([^\"']+)[\"']");
    public static final Pattern YOUTUBE_PATTERN = Pattern.compile("(?:https?:\\/\\/)?(?:www\\.)?(?:youtube\\.com\\/(?:[^\\/\\n\\s]+\\/\\S+\\/|(?:v|e(?:mbed)?)\\/|\\S*\\?v=)|youtu\\.be\\/)([a-zA-Z0-9_-]{11})", Pattern.CASE_INSENSITIVE);

    // SYSTEM PROPERTIES
    public static final String BASE_FOLDER_PROPERTY = "video.training.basefolder";
    public static final String FOLDER_HIDDEN_WITH_ACCESS_PROPERTY = "video.training.folder.hidden.withaccess";
    public static final String GLOBAL_ROOT_BASE_FOLDER_PROPERTY = "video.training.global.root.basefolder";
    public static final String GLOBAL_ROOT_PROPERTY = "video.training.global.root";
    public static final String HLS_ENABLED_PROPERTY = "video.training.hls.enabled";
    public static final String HLS_FFMPEG_PROPERTY = "video.training.hls.ffmpeg";
    public static final String MANAGED_UPLOAD_OWNER_PROPERTY = "video.training.ownerId";
    public static final String MANAGED_UPLOAD_PROPERTY = "video.training.managed";
    public static final String MANAGED_UPLOAD_SCOPE_PROPERTY = "video.training.visibilityScope";
    public static final String MANAGED_UPLOAD_SITE_PROPERTY = "video.training.siteId";
    public static final String MAX_NATIVE_UPLOAD_SIZE_PROPERTY = "video.training.max.upload.size";
    public static final String MODERATION_ENABLED_PROPERTY = "video.training.moderation.enabled";
    public static final String OAUTH_ENCRYPTION_KEY_PROPERTY = "video.training.oauth.encryption.key";
    public static final String UI_SERVICE_PROPERTY = "ui.service";

    // EXTERNAL PROVIDERS
    // YouTube
    public static final String PROVIDER_YOUTUBE_API_KEY_PROPERTY = "video.training.provider.youtube.api.key";
    public static final String PROVIDER_YOUTUBE_AUTH_STATE_SESSION_KEY = "video.training.provider.youtube.oauth.state";
    public static final String PROVIDER_YOUTUBE_CATEGORY_ID_PROPERTY = "video.training.provider.youtube.category.id";
    public static final String YOUTUBE_PRIVACY_PRIVATE = "private";
    public static final String YOUTUBE_UPLOAD_SCOPE = "https://www.googleapis.com/auth/youtube.upload https://www.googleapis.com/auth/youtube";

    // EMAIL CONFIGURATIONS
    public static final String EMAIL_BROKEN_BODY_KEY = "video.training.email.broken.body";
    public static final String EMAIL_BROKEN_SUBJECT_KEY = "video.training.email.broken.subject";
    public static final String EMAIL_DESCRIPTION_UPDATED_BODY_KEY = "video.training.email.descriptionUpdated.body";
    public static final String EMAIL_DESCRIPTION_UPDATED_SUBJECT_KEY = "video.training.email.descriptionUpdated.subject";
    public static final String EMAIL_TITLE_UPDATED_BODY_KEY = "video.training.email.titleUpdated.body";
    public static final String EMAIL_TITLE_UPDATED_SUBJECT_KEY = "video.training.email.titleUpdated.subject";
    public static final String EMAIL_UPDATED_BODY_KEY = "video.training.email.updated.body";
    public static final String EMAIL_UPDATED_SUBJECT_KEY = "video.training.email.updated.subject";
    public static final String SMTP_EMAIL_SERVICE_PROPERTY = "smtp@org.sakaiproject.email.api.EmailService";

    private VideoTrainingConstants() {
        throw new IllegalStateException("Utility class");
    }
}
