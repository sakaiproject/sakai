# Sakai Kernel

Contains the basic services that are used throughout Sakai.

## API

All the kernel's services [API](https://en.wikipedia.org/wiki/API) / SPI are located here

## Implementation

Contains the implementations to the kernel's API and their associated spring wiring:
* Alias [implementation](kernel-impl/src/main/java/org/sakaiproject/alias/impl) | [components.xml](kernel-impl/src/main/webapp/WEB-INF/alias-components.xml)
* Antivirus [implementation](kernel-impl/src/main/java/org/sakaiproject/antivirus/impl) | [components.xml](kernel-impl/src/main/webapp/WEB-INF/antivirus-components.xml)
* Authz [implementation](kernel-impl/src/main/java/org/sakaiproject/authz/impl) | [components.xml](kernel-impl/src/main/webapp/WEB-INF/authz-components.xml)
* Cluster [implementation](kernel-impl/src/main/java/org/sakaiproject/cluster/impl) | [components.xml](kernel-impl/src/main/webapp/WEB-INF/cluster-components.xml)
* Component [implementation](kernel-impl/src/main/java/org/sakaiproject/component/impl) | [components.xml](kernel-impl/src/main/webapp/WEB-INF/component-components.xml)
* Conditions [implementation](kernel-impl/src/main/java/org/sakaiproject/conditions/impl) | [components.xml](kernel-impl/src/main/webapp/WEB-INF/conditions-components.xml)
* ConfigStore [implementation](kernel-impl/src/main/java/org/sakaiproject/config/impl) | [components.xml](kernel-impl/src/main/webapp/WEB-INF/configstore-components.xml)
* Content [implementation](kernel-impl/src/main/java/org/sakaiproject/content/impl) | [components.xml](kernel-impl/src/main/webapp/WEB-INF/content-components.xml)
* DB [implementation](kernel-impl/src/main/java/org/sakaiproject/db/impl) | [components.xml](kernel-impl/src/main/webapp/WEB-INF/db-components.xml)
* Email [implementation](kernel-impl/src/main/java/org/sakaiproject/email/impl) | [components.xml](kernel-impl/src/main/webapp/WEB-INF/email-components.xml)
* Entity [implementation](kernel-impl/src/main/java/org/sakaiproject/entity/impl) | [components.xml](kernel-impl/src/main/webapp/WEB-INF/entity-components.xml)
* Event [implementation](kernel-impl/src/main/java/org/sakaiproject/event/impl) | [components.xml](kernel-impl/src/main/webapp/WEB-INF/event-components.xml)
* Ignite [implementation](kernel-impl/src/main/java/org/sakaiproject/ignite) | [components.xml](kernel-impl/src/main/webapp/WEB-INF/ignite-components.xml)
* Memory [implementation](kernel-impl/src/main/java/org/sakaiproject/memory/impl) | [components.xml](kernel-impl/src/main/webapp/WEB-INF/memory-components.xml)
* MessageBundle [implementation](kernel-impl/src/main/java/org/sakaiproject/messagebundle/impl) | [components.xml](kernel-impl/src/main/webapp/WEB-INF/messagebundle-components.xml)
* Messaging [implementation](kernel-impl/src/main/java/org/sakaiproject/messaging/impl) | [components.xml](kernel-impl/src/main/webapp/WEB-INF/messaging-components.xml)
* Site [implementation](kernel-impl/src/main/java/org/sakaiproject/site/impl) | [components.xml](kernel-impl/src/main/webapp/WEB-INF/site-components.xml)
* Tasks [implementation](kernel-impl/src/main/java/org/sakaiproject/tasks/impl) | [components.xml](kernel-impl/src/main/webapp/WEB-INF/tasks-components.xml)
* Time [implementation](kernel-impl/src/main/java/org/sakaiproject/time/impl) | [components.xml](kernel-impl/src/main/webapp/WEB-INF/time-components.xml)
* Tool [implementation](kernel-impl/src/main/java/org/sakaiproject/tool/impl) | [components.xml](kernel-impl/src/main/webapp/WEB-INF/tool-components.xml)
* User [implementation](kernel-impl/src/main/java/org/sakaiproject/user/impl) | [components.xml](kernel-impl/src/main/webapp/WEB-INF/user-components.xml)
* Util [implementation](kernel-impl/src/main/java/org/sakaiproject/util/impl) | [components.xml](kernel-impl/src/main/webapp/WEB-INF/util-components.xml)

## Component Manager

Responsible for configuring and wiring the implementations of the services.
Spring is used to create the Sakai Application Context which is the parent Spring context used in Sakai.

## Util

Contains common utility classes that are used throughout Sakai, one such example is [FormattedText](api/src/main/java/org/sakaiproject/util/api/FormattedText.java). 

## Private

Contains utilities that are specific to the kernel, and it's operation such as Hibernate and Spring utility classes.

## Storage

Contains storage/persistence classes that are used by traditional Sakai services.
Much of the newer services use Hibernate for persistence and has replaced the need for these.

