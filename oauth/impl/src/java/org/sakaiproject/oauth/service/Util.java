/*
 * #%L
 * OAuth Implementation
 * %%
 * Copyright (C) 2009 - 2013 The Sakai Foundation
 * %%
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
 * #L%
 */
package org.sakaiproject.oauth.service;

import lombok.extern.slf4j.Slf4j;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthProblemException;

import org.sakaiproject.oauth.dao.ConsumerDao;
import org.sakaiproject.oauth.domain.Accessor;
import org.sakaiproject.oauth.domain.Consumer;

/**
 * @author Colin Hebert
 */
@Slf4j
public final class Util {

    private Util() {
    }

    public static OAuthAccessor convertToOAuthAccessor(Accessor accessor, OAuthConsumer oAuthConsumer)
            throws OAuthProblemException {
        if (accessor == null)
            return null;
        if (!oAuthConsumer.consumerKey.equals(accessor.getConsumerId()))
            throw new OAuthProblemException(OAuth.Problems.CONSUMER_KEY_REFUSED);
        OAuthAccessor oAuthAccessor = new OAuthAccessor(oAuthConsumer);
        if (accessor.getType() == Accessor.Type.ACCESS)
            oAuthAccessor.accessToken = accessor.getToken();
        else
            oAuthAccessor.requestToken = accessor.getToken();
        oAuthAccessor.tokenSecret = accessor.getSecret();
        // Support Variable Accessor Secret http://wiki.oauth.net/w/page/12238502/AccessorSecret
        if (accessor.getAccessorSecret() != null)
            oAuthConsumer.setProperty(OAuthConsumer.ACCESSOR_SECRET, accessor.getAccessorSecret());
        return oAuthAccessor;
    }

    public static OAuthConsumer convertToOAuthConsumer(Consumer consumer) {
        if (consumer == null)
            return null;
        OAuthConsumer oAuthConsumer = new OAuthConsumer(consumer.getCallbackUrl(), consumer.getId(),
                consumer.getSecret(), null);
        // Support Accessor Secret http://wiki.oauth.net/w/page/12238502/AccessorSecret
        oAuthConsumer.setProperty(OAuthConsumer.ACCESSOR_SECRET, consumer.getAccessorSecret());
        return oAuthConsumer;
    }

    public static void importConsumers(ConsumerDao source, ConsumerDao destination) {
        for (Consumer consumer : source.getAll()) {
            try {
                destination.create(consumer);
                log.info("New consumer imported '" + consumer.getId() + "'");
            } catch (Exception e) {
                log.warn("Impossible to import '" + consumer.getId() + "' as a consumer.", e);
            }
        }
    }
}
