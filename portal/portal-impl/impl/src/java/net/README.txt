This has been pulled into the Sakai source tree because 
the code has not been maintained for over 4 years and there
are some real flaws in the code.

The biggest problem to solve is dealing with User Agent
values that simply increment a version number.  

The "Loose" matching should do this - but instead does
(did) a really poor successive match that threw away 
bits of the user agent - to the point where it effectively
picks a random device - I changed this to eliminate
any numbers and then do a match of the agents.

Also this should log rather than using System.out.println

- Chuck Sun Apr 22 00:55:16 EDT 2007
