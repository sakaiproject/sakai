User Detail Provider
====================

This provider allows deployments to configure a method to lookup additional
data about users. This is aimed at providing candidate numbers associated 
with users, student numbers, and additional notes about a user that are relevant to someone
marking an assessment.

The API that this provider is built upon in in the kernel
and is `org.sakaiproject.user.api.CandidateDetailsProvider`. This provider
can allow code to lookup details on a user and also to know if it additional
features are enabled for a particular site.

It may well be the case that a deployment doesn't have a
CandidateDetailProvider configured and all calling code should cope with the
bean not being set. Currently the assignments tool has been updated to use
the provider.

In a typical deployment when a user is loaded the additional data about the
user's candidate number, student number, and additional notes will also be loaded. This data
is then available for the provider to get from the user object when asked.
Alternatively the provider can do the lookup for the additional data when
asked, if this is the case then some caching may need to be put in place as
the current implementation doesn't do any caching as the user objects are 
well cached.

Encryption
----------

Currently the example implementation encrypts data it stores on the user object
so that if the object is accidentally serialised over HTTP the values that 
should remain secret aren't exposed. There is an option to not encrypt student numbers.
