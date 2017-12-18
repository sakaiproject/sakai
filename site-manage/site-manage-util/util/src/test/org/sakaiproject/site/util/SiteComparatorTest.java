/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2012 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.site.util;

import java.io.UnsupportedEncodingException;
import java.util.Vector;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Locale;

import lombok.extern.slf4j.Slf4j;
import junit.framework.TestCase;

import org.sakaiproject.util.SortedIterator;
import org.sakaiproject.site.util.SiteConstants;
import org.sakaiproject.site.util.Participant;

/**
 * This test was created to verify fix for lastname sorting issue at https://jira.
 * sakaiproject.org/browse/SAK-21745
 */
@Slf4j
public class SiteComparatorTest extends TestCase {

        public SiteComparatorTest(String name) {
                super(name);
        }

        public void testSiteComparatorByNameForUSLocale() {
                log.info("-----------------ES");
                executeTest(new java.util.Locale("es", "ES"));
        }

        public void testSiteComparatorByNameForESLocale() {
                log.info("-----------------US");
                executeTest(java.util.Locale.US);
        }

        private void executeTest(Locale locale) {
        	List<String> words = Arrays.asList("Äbc", "äbc", "Àbc", "àbc", "Abc", "abc", "ABC");
                //Locale.setDefault(locale);
                List participants = new Vector();
                String name1 = "Martinez Torcal, Josh";
                String name6 = "Test, Ima";
                String name2 = "Martin Troncoso, Fred";
                String name4 = "deJesus, Edwardo";
                String name3 = "De Silva, Mary";
                String name7 = "de Silva, Joan";
                String name9 = "de Silvá, Mark";  // <-- accented A
                String name5 = "Wílkes, Steven";
                String name8 = "Wilkes-Barre, Leslie"; // <--- accented I
                participants.add(getPartcipant(name1));
                participants.add(getPartcipant(name2));
                participants.add(getPartcipant(name3));
                participants.add(getPartcipant(name4));
                participants.add(getPartcipant(name5));
                participants.add(getPartcipant(name6));
                participants.add(getPartcipant(name7));
                participants.add(getPartcipant(name8));
                participants.add(getPartcipant(name9));
                
                for (Iterator wordItr = words.iterator(); wordItr.hasNext();) {
                	participants.add(getPartcipant((String) wordItr.next()));                
                }

                // uncomment to display sort order in stdout
                showParticipantOrder("***** Unsorted", participants);

                assertEquals(16, participants.size());
                Participant sprt = (Participant) participants.get(0);
                assertTrue(name1, name1.equals(sprt.getName()));

                String sortedBy = SiteConstants.SORTED_BY_PARTICIPANT_NAME;

                String sortedAsc = "true";
                Iterator sortedParticipants = null;
                sortedParticipants = new SortedIterator(participants.iterator(), new SiteComparator(sortedBy, sortedAsc, locale));
                participants.clear();

                while (sortedParticipants.hasNext()) {
                        Participant prt = (Participant) sortedParticipants.next();
                        participants.add(prt);
                }

                // uncomment to display sort order in stdout
                showParticipantOrder("***** Sorted", participants);
                Participant ckPrt = (Participant) participants.get(11);
                assertTrue(name2, name2.equals(ckPrt.getName()));

        }

        private Participant getPartcipant(String name) {
                Participant participant = new Participant();
                participant.name = name;
                return participant;
        }

        private void showParticipantOrder(String header, List participants) {
                log.info(header);
                java.io.PrintStream out;
                try {
                        for (Iterator itr = participants.iterator(); itr.hasNext();) {
                                Participant prt = (Participant) itr.next();
                                String name = prt.getName();
                                out = new java.io.PrintStream(System.out, false, "UTF-8");
                                out.println(name);
                        }

                } catch (UnsupportedEncodingException e) {
                        // do something
                }

        }

        private void setNewEnvironmentHack(Map<String, String> newenv)  {
        	try {
                Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
                Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
                theEnvironmentField.setAccessible(true);
                Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
                env.clear();
                env.putAll(newenv);
                Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
                theCaseInsensitiveEnvironmentField.setAccessible(true);
                Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
                cienv.clear();
                cienv.putAll(newenv);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
        }

}
