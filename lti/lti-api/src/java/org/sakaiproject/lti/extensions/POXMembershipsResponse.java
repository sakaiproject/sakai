/**
 * Copyright (c) 2011-2016 The Apereo Foundation
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
package org.sakaiproject.lti.extensions;

import java.io.Reader;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import lombok.extern.slf4j.Slf4j;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

@Slf4j
public class POXMembershipsResponse {

    public static final String UNSPECIFIED = "unspecified";

    private MembershipsHandler handler = new MembershipsHandler();

    public POXMembershipsResponse(Reader reader) {

        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(new InputSource(reader), handler);
            reader.close();
        } catch (Exception e) {
            log.error("Failed to parse memberships xml.", e);
        }
    }

    public List<Member> getMembers() {
        return handler.members;
    }

    public Map<String,List<Member>> getGroups() {
        return handler.groups;
    }

    private class MembershipsHandler extends DefaultHandler {

        private static final String MEMBER = "member";
        private static final String USER_ID = "user_id";
        private static final String PERSON_NAME_GIVEN  = "person_name_given";
        private static final String PERSON_NAME_FAMILY  = "person_name_family";
        private static final String PERSON_CONTACT_EMAIL_PRIMARY = "person_contact_email_primary";
        // Sakai uses role rather than roles, incorrectly.
        private static final String ROLE = "role";
        private static final String ROLES = "roles";
        private static final String GROUP = "group";
        private static final String SET = "set";
        private static final String TITLE = "title";

        private boolean grab = false;
        private boolean inGroup = false;
        private boolean inSet = false;
        private boolean inGroupTitle = false;

        private StringBuilder builder = new StringBuilder();

        private List<Member> members = new ArrayList<Member>();
        private Map<String,List<Member>> groups = new HashMap<String,List<Member>>();

        private Member currentMember = null;

        public void startElement(String uri, String localName, String qName, Attributes attributes) {

            if(log.isDebugEnabled()) log.debug("qName: {}", qName);

            grab = true;

            if (MEMBER.equals(qName)) {
                currentMember = new Member();
            } else if (USER_ID.equals(qName)) {
                builder = new StringBuilder();
            } else if (PERSON_NAME_GIVEN.equals(qName)) {
                builder = new StringBuilder();
            } else if (PERSON_NAME_FAMILY.equals(qName)) {
                builder = new StringBuilder();
            } else if (PERSON_CONTACT_EMAIL_PRIMARY.equals(qName)) {
                builder = new StringBuilder();
            } else if (ROLE.equals(qName)) {
                builder = new StringBuilder();
            } else if (ROLES.equals(qName)) {
                builder = new StringBuilder();
            } else if (GROUP.equals(qName)) {
                inGroup = true;
            } else if (SET.equals(qName)) {
                inSet = true;
            } else if (TITLE.equals(qName) && inGroup && !inSet) {
                builder = new StringBuilder();
                inGroupTitle = true;
            } else {
                grab = false;
            }
        }

        public void endElement(String uri, String localName, String qName) {

            if (MEMBER.equals(qName)) {
                if (currentMember.role != UNSPECIFIED) {
                    members.add(currentMember);
                } else {
                    // No role specified. This is incorrect.
                    log.warn("No role specified for member '{} {}'. Omitting from the list ...", currentMember.firstName, currentMember.lastName);
                }
            } else if (USER_ID.equals(qName)) {
                currentMember.userId = builder.toString();
            } else if (PERSON_NAME_GIVEN.equals(qName)) {
                currentMember.firstName = builder.toString();
            } else if (PERSON_NAME_FAMILY.equals(qName)) {
                currentMember.lastName = builder.toString();
            } else if (PERSON_CONTACT_EMAIL_PRIMARY.equals(qName)) {
                currentMember.email = builder.toString();
            } else if (ROLE.equals(qName)) {
                currentMember.role = builder.toString();
            } else if (ROLES.equals(qName)) {
                String rolesString = builder.toString();
                String[] roles = rolesString.split(",");
                if (roles.length > 0) {
                    currentMember.role = roles[0].trim();
                }
            } else if (GROUP.equals(qName)) {
                inGroup = false;
            } else if (SET.equals(qName)) {
                inSet = false;
            } else if (TITLE.equals(qName) && inGroupTitle) {

                String groupTitle = builder.toString();

                if (!groups.containsKey(groupTitle)) {

                    List<Member> groupMembers = new ArrayList<Member>();
                    groupMembers.add(currentMember);
                    groups.put(groupTitle, groupMembers);
                } else {

                    if(log.isDebugEnabled()) log.debug("Adding {} to {}", currentMember.userId, groupTitle);

                    groups.get(groupTitle).add(currentMember);
                }

                inGroupTitle = false;
            }
        }

        public void characters(char[] ch, int start, int length) {

            if (!grab) return;

            builder.append(ch, start, length);
        }
    }

    public class Member {

        public String userId = "";
        public String firstName = "";
        public String lastName = "";
        public String email = "";
        public String role = UNSPECIFIED;
    }
}
