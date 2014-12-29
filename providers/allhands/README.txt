Welcome to the experimental All Hands provider.

The goal of this code is to allow the creation of Sites in Sakai to which 
all users are automatically added.  

The basic idea is to "provide" an external group provider id called "sakai.allhands" and then 
either product a GroupProvider that indicates that all users are members of that group
or add the code to an existing group provider.

The main trick is in this method:

        public Map getGroupRolesForUser(String userId)
        {       
                Map rv = new HashMap();
                
                rv.put("sakai.allhands","access"); // Add this line
                
                return rv;
        }

This code can be added to you r existing GroupProvider if you have one. 

To enable this group provider, simply edit the file

../component/src/webapp/WEB-INF/components.xml

And uncomment the appropriate entry.

There are print statements in the code so you can be confident of that is 
happening.  Remove those statements before you go to production.  This is just 
a sample.

HOW TO USE THIS

Make a new site.  Either Setup or WorkSite Setup can be used.

Log in as Admin.  Use the Sites Tool to find the site you just added.  Grab the site ID
using copy.

Go to the realm's tool and past in the site ID and press "search" you will find the 
realm associated with the site.

Click on the realm - In the field "Provider Id" Enter "sakai.allhands" (no quotes) and save.

Now as people are logged in they get added to this site as "access".  If you change someone 
to maintain - they keep maintain.

WHY THIS WORKS?

When the user logs in, as part of their login the provider method

 getGroupRolesForUser(String userId)

The real question being asked here is "For this user, what is the list 
of Provider IDs does this person belong to and what roles does that 
user have for each ID".  We indicate that "for all the sites with 
sakai.allhands the current user deserves access".  So authzGroups 
above us does magic SQL to make this so it looks for all of the sites 
with the provider ID "sakai.allhands" and simply pokes 
the user into those sites.

Look in this file:

authz-impl/impl/src/java/org/sakaiproject/authz/impl/DbAuthzGroupService.java

Line 1554 (otr thereabouts) to see the fun:

  // for each realm that has a provider in the map, and does not have a grant for the user,
  // add the active provided grant with the map's role.

/Chuck
Sat Sep  9 00:34:37 CEST 2006

