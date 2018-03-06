# Chat
https://jira.sakaiproject.org/browse/SAK-32035

## Changes
* Removed some cover uses and replaced by spring injection
* Added lombok
* New Data structures based on portal-chat
  - Hearbeat (MAP[CHANNEL_ID][SESSION_ID] -> TransferableChatMessage) : This map will store who is alive in each channel. We store the session_id to allow login multiple times with different browsers. This data structure uses Guava cache to automatically expire data when no accessed/writted. The logic is :
    - All channel_id related data will be removed if nobody reads it in pollIntervalx2 time.
    - All session_id related data will be removed if nobody writes it in pollIntervalx2 time. (When adding a heartbeat, this data will be written).
  - MessageMap (MAP[SESSION_KEY][CHANNEL_ID] -> List<TransferableChatMessage>) : This map will store all messages pending to deliver for each session_key (ussage_session_id:session_user_id) in each channel. This data structure uses Guava cache to automatically expire data when no accessed. If nobody access to a given session_key in 5 minutes, that data will be removed (We assume the pollInterval will occur much much often than 5 minutes and that data will be accessed, consumed and manually removed). This just make us sure that messages sended and not delivered (because the user has left the channel/tool) will be automatically deleted. 
* New custom action "chatData" added to ChatMessageEntityProvider : this action will sustitute the current courier.
  - This action is supposed to be called every pollInterval time.
  - When called, update the heartbeat for the caller (if I'm asking for data, this means I'm alive).
  - Return undelivered messages for the current user and clean them all (once consumed we do not need them anymore). Messages can be : chat message or remove message.
  - Get all present and online users in the given channel.
* Added new fields to "SimpleChatMessage" (entity used by ChatMessageEntityProvider) : boolean removeable, MessageDateString messageDateString (String pdate, String ptime, String pid). Also upgraded with lombok.
* Using jGroups to communicate cluster nodes. jGroups messages allow us to synchronize the data structures (heartbeat + messageMap) between nodes.
* Added a default jGroups config xml file (**jgroups-config.xml**). This file is provided with the tool and will avoid problems with portal chat default configuration (uses a different multicast IP than the default). Anyway, this file can be overwritten setting a file called **jgroups-chat-config.xml** in SAKAI_HOME folder.
* Removed courier references : All uses of the courier service have been removed.
* Removed EventTracking and Presence Observers : We are not listening anymore the EventTracking or Presence events. These events where used to synchonize cluster nodes. This is not needed because of jGroups.
* Removed custom action "listen" from ChatChannelEntityProvider. This action was used to set a channel listener (appart from the used by the chat). Now, you can listen any channel by simply calling periodically "chatData" (of course you need the right permissions to do that).
* Improved GUI : Auto-scroll feature

## New Sakai properties
* chat.pollInterval (default 5000) : Time (in milliseconds) between each AJAX poll. This is also used to automatically expire guava cache data.
* chat.cluster.channel : jGroups channel id where the messages are going to be delivered. CARE: at this point, this value must be different from "portalchat.cluster.channel".

## Important JVM property
Make sure the java property **-Djava.net.preferIPv4Stack=true** is set. If not, jGroups may have problems delivering messages through network.

## Future improvements
- Move "sendToCluster" function to an external service like MesageService.
- Create a wrapper class in an external service (like MesageService). This class will be used to send generic messages between nodes in a cluster using jGroups. All tools/services who want to be synchonized in the cluster must extend this wrapper class. In our case, we will do this for Chat::transferableChatMessage and PortalChat::UserMessage
- Use the same jGroups channel for all tools/services (thanks to the wrapper class). This will remove some specific properties like portalchat.cluster.channel and chat.cluster.channel