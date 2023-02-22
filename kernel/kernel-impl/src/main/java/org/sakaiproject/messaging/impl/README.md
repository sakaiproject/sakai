# User Messaging

Sakai now has a user messaging service that handles email and browser push. A
tool can use this api to send an email or add to a digest. The service also
supports a list of user notification handlers to handle events from the event
bus (EventTrackingService). User notifications result in a browser push so any
browsers that have opted in will receive a push event and that will be
displayed in their bullhorn alerts. One user can have many push endpoints, one
for each device, and all those will receive a push.

## Email

A tool registers email templates, one for each type of email, and uses this api
to send a message key and a map with the values to insert into the template.
This map would also include the translations needed to internationalise the email.

## Push

Push is triggered by Sakai events, and events are only pushed to the browser if
a tool has registered a handler for that event. Also, events are only pushed to
a browser if the user has accepted those events.

### Configuring Push

Browser push uses public key encryption to secure the push messages, and the key
pair needs to be generated. OOTB, Sakai will generate these for you if they are
not in the Sakai home directory already - you still need to specify a subject
though. You could allow Sakai to create a keypair on one node and then copy
those keys over scp to your other Sakai nodes. If you want to setup your own
keys, you will need to generate a
[VAPID] (https://datatracker.ietf.org/doc/html/draft-thomson-webpush-vapid)
keypair. Fortunately, this is pretty easy - you can use an online generator for
these and [here is one] (https://vapidkeys.com/). The email you specify is
called the "sub" in the push service call, and you'll need that for your Sakai
properties. Once you have your keypair, save them in your sakai home directory,
under your Tomcat. The default, but configurable, names for these are
sakai\_push.key and sakai\_push.key.pub. You will need to set your subject email
in your Sakai properties.

Once you've configured your push settings, enable push by setting
portal.notifications.push.enabled to true, and restart Sakai.

## Using Push on the client

Notifications need to be permitted on the client browser. You know those
annoying requests you ignore all the time - you'll see one of those when you
click the bullhorns icon. Accept them from Sakai. Or don't - your choice :)

If you want to adapt a tool to consume push events on the client, you will need
code something like this in your tool's js. Let's use assignments as an example:

    portal.notifications.setup.then(() => {

        portal.notifications.registerPushCallback("sakai.assignment.grades", () => {

            console.log("Don't ... Push ... Me");

            ... more code ...
        });
    });

Assignments does push events. For instance - when you submit an assignment as a
student a push event goes out from Sakai to the push service.

## Properties

### portal.notifications.push.enabled

Push is currently on by default. To disable it, set this to false.
### portal.notifications.push.publickey

If you're not happy with the default of sakai\_push.key.pub, set it with this property.
### portal.notifications.push.privatekey

If you're not happy with the default of sakai\_push.key, set it with this property.
### portal.notifications.push.subject

You have to set this. Use the email that you specified when generating your
VAPID keypair.
