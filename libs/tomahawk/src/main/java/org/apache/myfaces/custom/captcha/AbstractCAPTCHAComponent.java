/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.custom.captcha;

import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.ExternalContext;
import org.apache.myfaces.tomahawk.config.TomahawkConfig;

/**
 * 
 * @JSFComponent
 *   name = "t:captcha"
 *   class = "org.apache.myfaces.custom.captcha.CAPTCHAComponent"
 *   tagClass = "org.apache.myfaces.custom.captcha.CAPTCHATag"
 * @since 1.1.7
 * @author Hazem Saleh
 *
 */
public abstract class AbstractCAPTCHAComponent extends UIComponentBase {

    public static String COMPONENT_TYPE = "org.apache.myfaces.CAPTCHA";
    public static String COMPONENT_FAMILY = "org.apache.myfaces.CAPTCHA";
    public static String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.CAPTCHA";
    
    // Value binding constants
    public static final String ATTRIBUTE_CAPTCHA_SESSION_KEY_NAME = "captchaSessionKeyName";
    public static final String ATTRIBUTE_IMAGE_WIDTH = "imageWidth";
    public static final String ATTRIBUTE_IMAGE_HEIGHT = "imageHeight";
    
    /**
     * Determines the CAPTCHA session key name. If
     * org.apache.myfaces.tomahawk.PREFIX_CAPTCHA_SESSION_KEY is set to true (default),
     * a prefix defined by the component is added to this key name.
     * 
     * @JSFProperty
     * @return
     */
    public abstract String getCaptchaSessionKeyName();
    
    /**
     * Integer to indicate the CAPTCHA width. default is 290.
     * 
     * @JSFProperty
     */
    public abstract String getImageWidth();

    /**
     * Integer to indicate the CAPTCHA height. default is 81.
     * 
     * @JSFProperty
     */
    public abstract String getImageHeight();

    /**
     * Return the value stored in session map related to captchaSessionKeyName
     * 
     * @return 
     */
    public String getCaptchaSessionValue()
    {
        //Return the value stored
        ExternalContext externalContext = getFacesContext().getExternalContext();
        TomahawkConfig config = TomahawkConfig.getCurrentInstance(externalContext);
        if (externalContext.getSession(false) != null)
        {
            return (String) getFacesContext().getExternalContext().getSessionMap().get(
                    config.isPrefixCaptchaSessionKey() ? 
                        ATTRIBUTE_CAPTCHA_SESSION_KEY_NAME+"_"+getCaptchaSessionKeyName() : 
                        getCaptchaSessionKeyName());
        }
        return null;
    }
}
