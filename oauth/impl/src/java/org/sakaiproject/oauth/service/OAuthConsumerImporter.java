package org.sakaiproject.oauth.service;

import org.sakaiproject.oauth.dao.ConsumerDao;
import org.sakaiproject.oauth.dao.ServerConfigConsumerDao;

public class OAuthConsumerImporter {

    private ServerConfigConsumerDao serverConfigConsumerDao;
    private ConsumerDao consumerDao;

    public void setServerConfigConsumerDao(
            ServerConfigConsumerDao serverConfigConsumerDao) {
        this.serverConfigConsumerDao = serverConfigConsumerDao;
    }

    public void setConsumerDao(ConsumerDao consumerDao) {
        this.consumerDao = consumerDao;
    }

    public void init() {
        Util.importConsumers(serverConfigConsumerDao, consumerDao);
    }
}