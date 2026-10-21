# Apache Ignite (https://ignite.apache.org/)

Sakai uses Apache Ignite's project to provide a toolkit for performing mechanics
that are important when multiple nodes are installed otherwise commonly referred
to as a cluster. 

## Hibernate Second Level Caching

Sakai uses Hibernate's `hibernate-jcache` module at exactly the same version as
`hibernate-core`. The global JPA factory selects Hibernate's stock `jcache`
region factory. There is no Ignite-specific Hibernate adapter.

The kernel owns one stock Ignite JCache manager. Its configuration URI retrieves
the already assembled `IgniteConfigurationAdapter` bean through ComponentManager;
it retains Sakai's discovery, classloader, cache and data-region settings. The
Spring cache manager and other Ignite consumers use that manager's unwrapped
node. `SharedJCacheManager` gives Hibernate a borrowed view: closing a factory or
a region does not close the owning manager, shared cache handles or node. Spring
closes the owner after dependent consumers. This facade uses only public JCache
interfaces and does not subclass Hibernate's internal implementation classes.

Hibernate's missing-cache strategy is `fail`. Declare entity, collection and
custom query regions in `ignite-components.xml`, including regions used by
optional tools. Do not silently create provider-default caches: they would lose
Sakai's configured replication, expiry and data region.

### Upgrading from ignite-hibernate-ext

Stop every Sakai/Ignite node in the cluster before switching providers. Remove
the old adapter JAR from Tomcat's shared libraries and deploy the matching
`hibernate-jcache` JAR. Start the cluster with empty nonpersistent caches. Old and
new providers must not share populated cache regions during a rolling upgrade;
their cache keys and values are not a compatible wire format.

The normal entity/query regions retain their existing settings. Invalidation
timestamps use an eternal, replicated, synchronous cache in a separate data
region with page eviction disabled. Expiring or evicting those timestamps can
make cached query results stale; do not apply entity-cache TTL/eviction to them.

### Validation

Run the kernel configuration and lifecycle checks with Java 21 and loopback
sockets available:

```sh
mvn -pl kernel/kernel-impl -am -Dtest=HibernateCacheConfigurationTest,HibernateCacheLegacyConfigurationTest,HibernateCacheOwnershipTest -Dsurefire.failIfNoSpecifiedTests=false test
```

The configuration tests exercise real MODI and traditional component lookup,
including a provider URI loaded from a JAR. Ownership tests load the production
JCache owner wiring and global JPA factory definition with an isolated node and
HSQLDB. They cover shared node identity, two factories, entity/query cache hits,
factory-close and failed-bootstrap isolation, Spring shutdown, transactions,
and borrowed public JCache handles. Their local fixture uses eternal/FULL_SYNC
caches to isolate lifecycle behavior.

Before removing draft status, qualify the published dependency, full deployed
kernel startup/shutdown, all effective region mappings, and cross-process
invalidation under production entity/query expiry and FULL_ASYNC policies.
The local fixture does not establish distributed correctness or database-vendor
compatibility.

Caching in Hibernate is important as it improves the performance of Sakai.
Any time you can forgo a query being sent to the database will improve the speed
at which a response can be constructed and sent back to the client.

Hibernate has the notion of a first level, and a second level cache.

### First Level Cache
Hibernate attempts to wait until the end before sending updates to the database
this is known traditionally known as transaction write-behind. All of this happens
in a single database conversation/session.

### Second Level Cache
The second level cache spans multiple database conversations/sessions and pertains to
Entities. Entities are cached after having been loaded from the
database and stored in the second level cache where other sessions can use them vs
loading them from the database for a single node.

### Cluster with Individual L2 Caches
Every node in a Sakai cluster has their own individual cache which contain cached entities
that are loaded and possibly modified and then persisted. Other nodes will only become aware
of the modified entities once a ttl has expired for that entity. This causes nodes to show
different states of a given entity until entity expires from the cache it is reloaded from
the database.

### Cluster with a Distributed Cache
Add Ignite distributed memory cache as the cache provider, and we end up with a
cluster that has a synchronized second level cache among all nodes. No longer will there
be entities that are not synchronized with other nodes in the cluster. As hibernate loads
entities it is loading the entity for the all nodes and when it invalidates as a result
of changes to those entities it is doing so for all nodes in the cluster.

## Configuration

Configuring Apache Ignite is going to be based on local requirements so here we will
be covering the initial strategy but others will likely be added.

Ignite is configured out of the box for a small configuration here are the following
properties that can be used to configure ignite.

* `ignite.node` a globally unique node ID which survives node restarts. By default, this is the
  _serverId_ and is unique to each node.
* `ignite.name` a local instance name for the cluster. By default, this is the _serverName_
  and is the same for all nodes in the same Sakai Cluster. 
* `ignite.home` this configures the directory that ignite will use as it's work directory.
  It should be on the servers local disk. By default, this is _$SAKAI_HOME/ignite/serverId_ 
* `ignite.mode` this configures the node as a client or server mode. Server nodes participate
  in caching, compute execution, stream processing, etc., while client nodes provide
  the ability to connect to the servers remotely. By default, the mode is _server_.
* `ignite.address` this nodes address

  _10.1.1.100_
* `ignite.addresses` other nodes addresses and their discovery port ranges, careful not to use the
  tcp communication port range.

  _10.1.1.101:49010..49019,10.1.1.102:49010..49019_
* `ignite.port` this is the starting port used to calculate the range. By default, it is 0 and
  any value in the privileged port range will cause it to pick a port between 49152 and 65535. 
  
  _49000_
* `ignite.range` this is the number of ports that will be used when calculating the range. The range
  is divided into 2 sections the first 10 ports are for tcp communication between needs and the last
  10 ports are for discovery and are used by the config above ignite.addresses.

  _10_
* `ignite.tcpMessageQueueLimit` sets message queue limit for incoming and outgoing messages.
  This would only need to be changed if there is a slow node in the cluster.

  _1024_
* `ignite.tcpSlowClientMessageQueueLimit` sets message queue limit for outgoing messages for a remote client.
  This helps in detecting a slow node sooner and works in conjuction with ignite.tcpMessageQueueLimit.

  _512_ or (half of ignite.tcpMessageQueueLimit)

## Cache Data Regions
Sakai configures three data regions for Spring, Hibernate second-level data and
query-invalidation timestamps.

* Spring Region - this is the default region and caches will be added to this region.
* Hibernate L2 Region - this is hibernates second level cache
* Hibernate Timestamps Region - eternal query-invalidation metadata without page eviction

```xml
<bean class="org.apache.ignite.configuration.DataRegionConfiguration">
    <property name="name" value="spring_region"/>
    <property name="initialSize" value="#{10 * 1024 * 1024}"/>
    <property name="maxSize" value="#{100 * 1024 * 1024}"/>
    <property name="pageEvictionMode" value="RANDOM_2_LRU"/>
    <property name="persistenceEnabled" value="false"/>
    <property name="metricsEnabled" value="false"/>
</bean>
<bean class="org.apache.ignite.configuration.DataRegionConfiguration">
    <property name="name" value="hibernate_l2_region"/>
    <property name="initialSize" value="#{300 * 1024 * 1024}"/>
    <property name="maxSize" value="#{600 * 1024 * 1024}"/>
    <property name="pageEvictionMode" value="RANDOM_2_LRU"/>
    <property name="persistenceEnabled" value="false"/>
    <property name="metricsEnabled" value="false"/>
</bean>
```

Here is a sample of the 2 regions from the node command:
```text
Data region metrics:
+=============================================================================================================================+
|        Name         | Page size |       Pages        |    Memory     |      Rates       | Checkpoint buffer | Large entries |
+=============================================================================================================================+
| hibernate_l2_region | 0         | Total:  46195      | Total:  182mb | Allocation: 0.00 | Pages: 0          | 0.00%         |
|                     |           | Dirty:  0          | In RAM: 182mb | Eviction:   0.00 | Size:  0          |               |
|                     |           | Memory: 46195      |               | Replace:    0.00 |                   |               |
|                     |           | Fill factor: 0.00% |               |                  |                   |               |
+---------------------+-----------+--------------------+---------------+------------------+-------------------+---------------+
| spring_region       | 0         | Total:  0          | Total:  0     | Allocation: 0.00 | Pages: 0          | 0.00%         |
|                     |           | Dirty:  0          | In RAM: 0     | Eviction:   0.00 | Size:  0          |               |
|                     |           | Memory: 0          |               | Replace:    0.00 |                   |               |
|                     |           | Fill factor: 0.00% |               |                  |                   |               |
+---------------------+-----------+--------------------+---------------+------------------+-------------------+---------------+
```

## Cache Configuration

* Ignite must know about the caches that it will be managing. The caches are
  declared [here](../../../../webapp/WEB-INF/ignite-components.xml)

  There are 2 types of caches that we will be using an Atomic and a Transactional as can be
  seen in the following bean definitions:
  ```xml
  <bean id="org.sakaiproject.ignite.cache.atomic" class="org.apache.ignite.configuration.CacheConfiguration" abstract="true">
      <property name="atomicityMode" value="ATOMIC"/>
      <property name="cacheMode" value="REPLICATED"/>
      <property name="writeSynchronizationMode" value="PRIMARY_SYNC"/>
      <property name="dataRegionName" value="hibernate_l2_region"/>
      <property name="onheapCacheEnabled" value="false"/>
  </bean>

  <bean id="org.sakaiproject.ignite.cache.transactional" class="org.apache.ignite.configuration.CacheConfiguration" abstract="true">
      <property name="atomicityMode" value="TRANSACTIONAL"/>
      <property name="cacheMode" value="REPLICATED"/>
      <property name="writeSynchronizationMode" value="PRIMARY_SYNC"/>
      <property name="dataRegionName" value="hibernate_l2_region"/>
      <property name="onheapCacheEnabled" value="false"/>
  </bean>
  ```
  Sakai's declared NONSTRICT_READ_WRITE mappings use the atomic cache parent.
  Hibernate implements the access strategy through JCache; do not infer a JTA
  cache transaction merely from Ignite's TRANSACTIONAL cache mode.
