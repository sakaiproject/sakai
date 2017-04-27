/**********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
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

package org.sakaiproject.rubrics.repository.custom;

import lombok.NoArgsConstructor;
import org.sakaiproject.rubrics.RubricsConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;


@BasePathAwareController
@RequestMapping(value="/translations")
@NoArgsConstructor
public class TranslationsController {

    @Autowired
    RubricsConfiguration rubricsConfiguration;

    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Translations translations(@RequestParam("lang-code") String langCode) {

        try {
            String sakaisession = getSakaiSession();
            Translations translations = getSakaiTranslation(langCode,sakaisession);
            return translations;

        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Returns the session string for admin user
     * @return
     */
    public String getSakaiSession() throws IOException {


        try{
            String adminUser = rubricsConfiguration.getIntegration().getSakaiAdminUser();
            String adminPassword = rubricsConfiguration.getIntegration().getSakaiAdminPassword();
            URL url = new URL(rubricsConfiguration.getIntegration().getSakaiRestUrl() + "login/login?id="+adminUser+"&pw="+adminPassword);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            String result="";
            if ((output = br.readLine()) != null) {
                result = output;
            }
            conn.disconnect();
            return result;

        } catch (MalformedURLException e) {

            //log.warn("Error getting a rubric association " + e.getMessage());
            return null;

        } catch (IOException e) {

            //log.warn("Error getting a rubric association" + e.getMessage());
            return null;
        }
    }

    /**
     * Returns the session string for admin user
     * @return
     */
    public Translations getSakaiTranslation(String langCode, String session) throws IOException {


        try{
            URL url = new URL(rubricsConfiguration.getIntegration().getSakaiRestUrl() + "i18n/getI18nProperties?sessionid=" + session + "&locale=" + langCode +  "&resourceclass=org.sakaiproject.rubrics.logic.api.RubricsService&resourcebundle=rubricsMessages");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");


            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            HashMap<String,String> labels = new HashMap<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            while ((output = br.readLine()) != null) {

                String key=output.split("=")[0];
                String value=output.split("=")[1];
                labels.put(key,value);
            }
            conn.disconnect();

            Translations translations = new Translations(langCode,labels);

            return translations;


        } catch (MalformedURLException e) {
            //log.warn("Error getting a rubric association " + e.getMessage());
            return null;

        } catch (IOException e) {
            //log.warn("Error getting a rubric association" + e.getMessage());
            return null;
        }
    }

    private class Translations{
        String langCode;
        HashMap<String,String> labels;

        Translations(String langCode, HashMap<String,String> labels){
            this.langCode=langCode;
            this.labels=labels;
        };

        public String getLangCode() {
            return langCode;
        }

        public void setLangCode(String langCode) {
            this.langCode = langCode;
        }

        public HashMap<String, String> getLabels() {
            return labels;
        }

        public void setLabels(HashMap<String, String> labels) {
            this.labels = labels;
        }




    }

}
