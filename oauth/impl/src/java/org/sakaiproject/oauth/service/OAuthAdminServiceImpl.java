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

import org.sakaiproject.oauth.dao.AccessorDao;
import org.sakaiproject.oauth.dao.ConsumerDao;
import org.sakaiproject.oauth.domain.Accessor;
import org.sakaiproject.oauth.domain.Consumer;

import java.util.Collection;

/**
 * @author Colin Hebert
 */
public class OAuthAdminServiceImpl implements OAuthAdminService {
    private OAuthService oauthService;
    private ConsumerDao consumerDao;
    private AccessorDao accessorDao;

    @Override
    public Consumer getConsumer(String consumerId) {
        return consumerDao.get(consumerId);
    }

    @Override
    public void createConsumer(Consumer consumer) {
        consumerDao.create(consumer);
    }

    @Override
    public Consumer updateConsumer(Consumer consumer) {
        return consumerDao.update(consumer);
    }

    @Override
    public void deleteConsumer(Consumer consumer) {
        Collection<Accessor> accessors = accessorDao.getByConsumer(consumer.getId());
        for (Accessor accessor : accessors) {
            oauthService.revokeAccessor(accessor.getToken());
        }
        consumerDao.remove(consumer);
    }

    @Override
    public Collection<Consumer> getAllConsumers() {
        return consumerDao.getAll();
    }

    @Override
    public void switchRecordMode(Consumer consumer) {
        consumer.setRecordModeEnabled(!consumer.isRecordModeEnabled());
        consumerDao.update(consumer);
    }

    public void setOauthService(OAuthService oauthService) {
        this.oauthService = oauthService;
    }

    public void setConsumerDao(ConsumerDao consumerDao) {
        this.consumerDao = consumerDao;
    }

    public void setAccessorDao(AccessorDao accessorDao) {
        this.accessorDao = accessorDao;
    }
}
