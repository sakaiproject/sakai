/**********************************************************************************
 *
 * Copyright (c) 2015 The Sakai Foundation
 *
 * Original developers:
 *
 *   New York University
 *   Payten Giles
 *   Mark Triggs
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.pasystem.api;

import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import lombok.Getter;

import static org.sakaiproject.pasystem.api.ValidationHelper.*;

/**
 * A data object representing a banner.
 */
public class Banner implements Comparable<Banner> {
    private final String uuid;
    @Getter
    private final String message;
    @Getter
    private final long startTime;
    @Getter
    private final long endTime;
    @Getter
    private final String hosts;
    private final BannerType type;
    @Getter
    private final boolean isActive;

    @Getter
    private final boolean isDismissed;

    enum BannerType {
        HIGH,
        MEDIUM,
        LOW
    }

    public Banner(String message, String hosts, boolean active, long startTime, long endTime, String type) {
        this(null, message, hosts, active, startTime, endTime, type, false);
    }

    public Banner(String uuid, String message, String hosts, boolean active, long startTime, long endTime, String type) {
        this(uuid, message, hosts, active, startTime, endTime, type, false);
    }

    public Banner(String uuid, String message, String hosts, boolean active, long startTime, long endTime, String type, boolean isDismissed) {
        this.uuid = uuid;
        this.message = message;
        this.hosts = hosts;
        this.isActive = active;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = BannerType.valueOf(type.toUpperCase(Locale.ROOT));
        this.isDismissed = isDismissed;
    }

    /**
     * The type of this banner (high, medium, low).
     */
    public String getType() {
        return this.type.toString().toLowerCase(Locale.ROOT);
    }

    /**
     * Determine the default acknowledgement type for this banner.
     */
    public AcknowledgementType calculateAcknowledgementType() {
        if (type.equals(BannerType.MEDIUM)) {
            return AcknowledgementType.TEMPORARY;
        } else {
            return AcknowledgementType.PERMANENT;
        }
    }

    @Override
    public int compareTo(Banner other) {
        return getSeverityScore() - other.getSeverityScore();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Banner)) {
            return false;
        }

        try {
            return uuid.equals(((Banner)obj).getUuid());
        } catch (MissingUuidException e) {
            return false;
        }
    }

    public int hashCode() {
        return uuid.hashCode();
    }

    /**
     * A numeric type representing this banner's importance (higher number = more important)
     */
    public int getSeverityScore() {
        return type.ordinal();
    }

    public String getUuid() throws MissingUuidException {
        if (this.uuid == null) {
            throw new MissingUuidException("No UUID has been set for this banner");
        }

        return this.uuid;
    }

    /**
     * Whether or not this banner should be displayed at this moment in time.
     */
    public boolean isActiveNow() {
        if (!isActive()) {
            return false;
        }

        if (startTime == 0 && endTime == 0) {
            return isActive();
        }

        Date now = new Date();

        return (now.after(new Date(startTime))
                && (endTime == 0 || now.before(new Date(endTime))));
    }

    /**
     * Whether or not this banner type can be dismissed by a user.
     */
    public boolean isDismissible() {
        return !BannerType.HIGH.equals(type);
    }

    /**
     * True if this banner is active now and assigned to the current server.
     */
    public boolean isActiveForHost(String hostname) {
        // are we active?
        if (!isActiveNow()) {
            return false;
        }

        // if no hosts then assume active for any host
        if (hosts == null || hosts.isEmpty()) {
            return true;
        }

        // we have some hosts defined, so check if the current is listed
        return Arrays.asList(hosts.split(",")).contains(hostname);
    }

    /**
     * Check that the values we've been given make sense.
     */
    public Errors validate() {
        Errors errors = new Errors();

        if (!startTimeBeforeEndTime(startTime, endTime)) {
            errors.addError("start_time", "start_time_after_end_time");
            errors.addError("end_time", "start_time_after_end_time");
        }

        return errors;
    }
}
