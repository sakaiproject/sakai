## What it does

The Public Announcement system provides:

  * The ability to display a banner message to users. These can be
    limited to particular servers and given different severity
    levels. Use cases include allowing administrators to alert users
    to upcoming system downtime, weather warnings and informational
    messages.

  * Popup messages on login. These can be used to show larger amounts
    of information in a modal dialog, and users can dismiss each
    message either temporarily (in which case it will be shown 24
    hours later), or permanently.

  * Timezone checks, which alert the user if the timezone set in their
    Sakai preferences doesn't match the timezone of their local
    machine. This warning is displayed as a banner alert and links the
    user to the page where they can set their timezone.


## Configuration

You can control the Public Announcement system with the following
properties:

  * pasystem.enabled (default: true) -- Whether or not to show popups
    and banners to users.

  * pasystem.auto.ddl (default: false) -- Whether to automatically
    create/upgrade the PA System's database tables on startup.  This
    is required for the first time you start the PA System, but
    there's no harm in leaving it enabled.

  * pasystem.banner.temporary-timeout-ms (default: 86400000) -- The
    number of milliseconds to hide a medium priority banner once it
    has been dismissed.

  * pasystem.popup.temporary-timeout-ms (default: 86400000) -- The
    "later" in "Remind me later" when a popup is dismissed.

  * pasystem.timezone-check (default: true) -- Whether to warn users
    via banner if their computer's timezone doesn't match their Sakai
    profile timezone.
