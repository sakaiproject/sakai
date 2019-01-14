/**
 * Copyright (c) 2008-2010 The Sakai Foundation
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

/**
 * Daniel Robinson (d.b.robinson@lancaster.ac.uk)
 * Adrian Fish (a.fish@lancaster.ac.uk) 
 */

// See http://confluence.sakaiproject.org/display/RSTR/Roster2
roster.RosterPermissions = function (permissions) {

    var self = this;
	
	permissions.forEach(function (p) {

		// roster permissions
		if ('roster.export' === p) {
			self.rosterExport = true;
		} else if ('roster.viewallmembers' === p) {
			self.viewAllMembers = true;
		} else if ('roster.viewenrollmentstatus' === p) {
			self.viewEnrollmentStatus = true;
		} else if ('roster.viewgroup' === p) {
			self.viewGroup = true;
		} else if ('roster.viewhidden' === p) {
			self.viewHidden = true;
		} else if ('roster.viewprofile' === p) {
			self.viewProfile = true;
		} else if ('roster.viewofficialphoto' === p) {
			self.viewOfficialPhoto = true;
		} else if ('roster.viewsitevisits' === p) {
			self.viewSiteVisits = true;
		} else if ('roster.viewemail' === p) {
			self.viewEmail = true;
		} else if ('site.upd' === p) { // sakai permissions
			self.siteUpdate = true;
		}
    });
};
