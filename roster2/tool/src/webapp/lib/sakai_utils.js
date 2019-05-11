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
 * Adrian Fish (a.fish@lancaster.ac.uk)
 */
(function ($) {

    roster.sakai = {
        setCurrentUserPermissions: function (siteId, callback) {

            $.ajax( {
                url: "/direct/site/" + siteId + "/userPerms.json",
                dataType: "json",
                cache: false,
                success: function (perms, status) {

                    roster.currentUserPermissions = new roster.RosterPermissions(perms.data);
                    callback();
                },
                error : function(xmlHttpRequest, stat, error) {
                    alert("Failed to get the current user permissions. Status: " + stat + ". Error: " + error);
                }
            });
        }
    };
}) (jQuery);
