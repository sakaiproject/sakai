/**
 * Copyright (c) 2010 onwards - The Sakai Foundation
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

function MeetingsPermissions(data) {

  if (data) {
    for (var i = 0, j = data.length; i < j; i++) {

      if ('meetings.admin' === data[i]) {
        this.meetingsAdmin = true;
      } else if ('meetings.create' === data[i]) {
        this.meetingsCreate = true;
      } else if ('meetings.edit.own' === data[i]) {
        this.meetingsEditOwn = true;
        this.meetingsViewMeetingList = true;
      } else if ('meetings.edit.any' === data[i]) {
        this.meetingsEditAny = true;
        this.meetingsViewMeetingList = true;
      } else if ('meetings.delete.own' === data[i]) {
        this.meetingsDeleteOwn = true;
        this.meetingsViewMeetingList = true;
      } else if ('meetings.delete.any' === data[i]) {
        this.meetingsDeleteAny = true;
        this.meetingsViewMeetingList = true;
      } else if ('meetings.participate' === data[i]) {
        this.meetingsParticipate = true;
        this.meetingsViewMeetingList = true;
      } else if ('meetings.recordings.view' === data[i]) {
        this.meetingsRecordingView = true;
      } else if ('meetings.recordings.edit.own' === data[i]) {
        this.meetingsRecordingEditOwn = true;
      } else if ('meetings.recordings.edit.any' === data[i]) {
        this.meetingsRecordingEditAny = true;
      } else if ('meetings.recordings.delete.own' === data[i]) {
        this.meetingsRecordingDeleteOwn = true;
      } else if ('meetings.recordings.delete.any' === data[i]) {
        this.meetingsRecordingDeleteAny = true;
      } else if ('meetings.recordings.extendedformats.own' === data[i]) {
        this.meetingsRecordingExtendedFormatsOwn = true;
      } else if ('meetings.recordings.extendedformats.any' === data[i]) {
        this.meetingsRecordingExtendedFormatsAny = true;
      } else if ('site.upd' === data[i]) {
        this.siteUpdate = true;
      } else if ('site.viewRoster' === data[i]) {
        this.siteViewRoster = true;
      } else if ('calendar.new' === data[i]) {
        this.calendarNew = true;
      } else if ('calendar.revise.own' === data[i]) {
        this.calendarReviseOwn = true;
      } else if ('calendar.revise.any' === data[i]) {
        this.calendarReviseAny = true;
      } else if ('calendar.delete.own' === data[i]) {
        this.calendarDeleteOwn = true;
      } else if ('calendar.delete.any' === data[i]) {
        this.calendarDeleteAny = true;
      }
    }
  }
}
