package org.sakaiproject.webapi.beans;

public class VideoTrainingAnalyticsRestBean {

    private String videoId;
    private long viewCount;
    private long uniqueViewerCount;

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public long getUniqueViewerCount() {
        return uniqueViewerCount;
    }

    public void setUniqueViewerCount(long uniqueViewerCount) {
        this.uniqueViewerCount = uniqueViewerCount;
    }
}
