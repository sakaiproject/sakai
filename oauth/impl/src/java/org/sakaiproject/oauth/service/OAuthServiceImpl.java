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

import org.joda.time.DateTime;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.oauth.advisor.CollectingPermissionsAdvisor;
import org.sakaiproject.oauth.advisor.LimitedPermissionsAdvisor;
import org.sakaiproject.oauth.dao.AccessorDao;
import org.sakaiproject.oauth.dao.ConsumerDao;
import org.sakaiproject.oauth.domain.Accessor;
import org.sakaiproject.oauth.domain.Consumer;
import org.sakaiproject.oauth.exception.*;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

/**
 * @author Colin Hebert
 */
public class OAuthServiceImpl implements OAuthService {
    private static final int VERIFIER_MAX_VALUE = 10000;
    private AccessorDao accessorDao;
    private ConsumerDao consumerDao;
    private boolean keepOldAccessors;
    private SiteService siteService;
    private SecurityService securityService;

    private static String generateToken(Accessor accessor) {
        // TODO Need a better way of generating tokens in the long run.
        return generateHash(accessor.getConsumerId() + System.nanoTime());
    }

    private static String generateSecret(Accessor accessor, Consumer consumer) {
        // TODO Need a better way of generating tokens in the long run.
        return generateHash(accessor.getToken() + consumer.getSecret() + System.nanoTime());
    }

    private static String generateHash(String string) {
        try {
            MessageDigest messageDigest;
            try {
                messageDigest = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                messageDigest = MessageDigest.getInstance("MD5");
            }
            byte[] hashBytes = messageDigest.digest(string.getBytes("UTF-8"));

            StringBuilder md5String = new StringBuilder();
            for (byte hashByte : hashBytes) {
                md5String.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
            }
            return md5String.toString();
        } catch (NoSuchAlgorithmException e) {
            // Unless you don't have md5 on your JVM it will work (so this exception won't happen)
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            // Unless you don't have UTF-8 on your JVM it will work (so this exception won't happen)
            throw new RuntimeException(e);
        }
    }

    private static String generateVerifier(Accessor accessor) {
        return String.valueOf(new Random().nextInt(VERIFIER_MAX_VALUE));
    }

    private static boolean isStillValid(Accessor accessor) {
        return accessor.getExpirationDate() == null || new DateTime(accessor.getExpirationDate()).isAfterNow();
    }

    public void setAccessorDao(AccessorDao accessorDao) {
        this.accessorDao = accessorDao;
    }

    public void setConsumerDao(ConsumerDao consumerDao) {
        this.consumerDao = consumerDao;
    }

    public void setKeepOldAccessors(boolean keepOldAccessors) {
        this.keepOldAccessors = keepOldAccessors;
    }

    @Override
    public Accessor getAccessor(String token, Accessor.Type expectedType) {
        Accessor accessor = accessorDao.get(token);

        if (accessor == null)
            throw new InvalidAccessorException("Accessor '" + token + "' doesn't exist.");

        if (accessor.getStatus() == Accessor.Status.VALID && !isStillValid(accessor))
            updateAccessorStatus(accessor, Accessor.Status.EXPIRED);

        if (accessor.getStatus() == Accessor.Status.EXPIRED)
            throw new ExpiredAccessorException("Accessor '" + token + " expired");
        else if (accessor.getStatus() == Accessor.Status.REVOKED)
            throw new RevokedAccessorException("Accessor '" + token + " revoked");
        else if (accessor.getStatus() != Accessor.Status.VALID)
            throw new InvalidAccessorException("Accessor '" + token + "' is not valid. (" + accessor.getStatus() + ")");

        if (accessor.getType() != expectedType)
            throw new InvalidAccessorException("Accessor with unexpected type " + accessor.getType());

        return accessor;
    }

    @Override
    public SecurityAdvisor getSecurityAdvisor(String accessorId) {
        Accessor accessor = getAccessor(accessorId, Accessor.Type.ACCESS);
        Consumer consumer = consumerDao.get(accessor.getConsumerId());
        if (consumer.isRecordModeEnabled())
            return new CollectingPermissionsAdvisor(consumerDao, consumer);
        else
            return new LimitedPermissionsAdvisor(consumer.getRights());
    }

    @Override
    public Consumer getConsumer(String consumerKey) {
        Consumer consumer = consumerDao.get(consumerKey);
        if (consumer == null)
            throw new InvalidConsumerException("Consumer '" + consumerKey + " doesn't exist");

        return consumer;
    }

    @Override
    public Accessor createRequestAccessor(String consumerId, String callback, String accessorSecret) {
        Consumer consumer = consumerDao.get(consumerId);
        Accessor accessor = new Accessor();
        accessor.setConsumerId(consumer.getId());
        accessor.setType(Accessor.Type.REQUEST);
        accessor.setStatus(Accessor.Status.VALID);
        accessor.setCreationDate(new DateTime().toDate());
        // A request accessor is valid for 15 minutes only
        accessor.setExpirationDate(new DateTime().plusMinutes(15).toDate());
        accessor.setAccessorSecret(accessorSecret);

        if (callback != null)
            accessor.setCallbackUrl(callback);
        else if (consumer.getCallbackUrl() != null)
            accessor.setCallbackUrl(consumer.getCallbackUrl());
        else
            accessor.setCallbackUrl(OUT_OF_BAND_CALLBACK);

        accessor.setToken(generateToken(accessor));
        accessor.setSecret(generateSecret(accessor, consumer));
        accessorDao.create(accessor);

        return accessor;
    }

    @Override
    public Accessor startAuthorisation(String accessorId) {
        Accessor accessor = getAccessor(accessorId, Accessor.Type.REQUEST);
        accessor.setVerifier(generateVerifier(accessor));
        accessor.setType(Accessor.Type.REQUEST_AUTHORISING);
        // The authorisation must be done in less than 15 minutes
        accessor.setExpirationDate(new DateTime().plusMinutes(15).toDate());
        accessor = accessorDao.update(accessor);
        return accessor;
    }

    @Override
    public Accessor authoriseAccessor(String accessorId, String verifier, String userId) {
        Accessor accessor = getAccessor(accessorId, Accessor.Type.REQUEST_AUTHORISING);
        if (!accessor.getVerifier().equals(verifier))
            throw new OAuthException("Accessor verifier invalid.");
        if (securityService.isSuperUser(userId))
            throw new OAuthException("Super users can't use OAuth for security reasons.");
        accessor.setVerifier(generateVerifier(accessor));
        accessor.setType(Accessor.Type.REQUEST_AUTHORISED);
        accessor.setUserId(userId);
        // An authorised request accessor is valid for one month only
        accessor.setExpirationDate(new DateTime().plusMonths(1).toDate());
        accessor = accessorDao.update(accessor);

        generateUserSite(userId);
        return accessor;
    }

    /**
     * Generate user's site if this is the very first login.
     *
     * @param userId
     */
    private void generateUserSite(String userId) {
        try {
            String userSiteId = siteService.getUserSiteId(userId);
            siteService.getSite(userSiteId);
        } catch (IdUnusedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Accessor createAccessAccessor(String requestAccessorId) {
        Accessor requestAccessor = getAccessor(requestAccessorId, Accessor.Type.REQUEST_AUTHORISED);

        Consumer consumer = consumerDao.get(requestAccessor.getConsumerId());
        Accessor accessAccessor = new Accessor();
        accessAccessor.setConsumerId(consumer.getId());
        accessAccessor.setUserId(requestAccessor.getUserId());
        accessAccessor.setType(Accessor.Type.ACCESS);
        accessAccessor.setStatus(Accessor.Status.VALID);
        accessAccessor.setCreationDate(new DateTime().toDate());

        // An access accessor is valid based on the number of minutes given by the consumer
        if (consumer.getDefaultValidity() > 0)
            accessAccessor.setExpirationDate(DateTime.now().plusMinutes(consumer.getDefaultValidity()).toDate());
        accessAccessor.setToken(generateToken(accessAccessor));
        accessAccessor.setSecret(generateSecret(accessAccessor, consumer));

        updateAccessorStatus(requestAccessor, Accessor.Status.EXPIRED);
        accessorDao.create(accessAccessor);

        return accessAccessor;
    }

    @Override
    public void denyRequestAccessor(String accessorId) {
        try {
            Accessor accessor = getAccessor(accessorId, Accessor.Type.REQUEST_AUTHORISING);
            updateAccessorStatus(accessor, Accessor.Status.REVOKED);
        } catch (OAuthException ignored) {
            // If the accessor is already expired/revoked, nothing to do/handle
        }
    }

    @Override
    public Collection<Accessor> getAccessAccessorForUser(String userId) {
        Collection<Accessor> accessors = new ArrayList<Accessor>(accessorDao.getByUser(userId));

        for (Iterator<Accessor> iterator = accessors.iterator(); iterator.hasNext();) {
            Accessor accessor = iterator.next();

            if (accessor.getStatus() == Accessor.Status.VALID && !isStillValid(accessor))
                updateAccessorStatus(accessor, Accessor.Status.EXPIRED);

            if (accessor.getStatus() != Accessor.Status.VALID
                    || accessor.getType() != Accessor.Type.ACCESS)
                iterator.remove();
        }
        return accessors;
    }

    @Override
    public void revokeAccessor(String accessorId) {
        try {
            Accessor accessor = getAccessor(accessorId, Accessor.Type.ACCESS);
            updateAccessorStatus(accessor, Accessor.Status.REVOKED);
        } catch (OAuthException ignored) {
            // If the accessor is already expired/revoked, nothing to do/handle
        }
    }

    private void updateAccessorStatus(Accessor accessor, Accessor.Status status) {
        if (keepOldAccessors || status == Accessor.Status.VALID) {
            accessor.setStatus(status);
            accessorDao.update(accessor);
        } else
            accessorDao.remove(accessor);
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }
}
