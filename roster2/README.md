# Roster2

Roster2 is a replacement for Sakai's Roster tool. The tool displays information
about a worksite's membership and enrolments, including things like group
membership and roles. Roster2 is capable of scaling to thousands of site members
by virtue of just in time loading (infinite scroll) and client side rendering.

## Development

If you want to develop Roster2, here's how to go about it:

1. Fork the Sakai repo then clone your fork to your local dev machine.
2. Submit a ticket to https://jira.sakaiproject.org with the component 'Roster2'
3. Branch the master locally using the name of the just created ticket.
4. Code your changes in the roster directory, test, commit and then push the
branch to your fork.
5. When you're happy, submit a pull request for review.

### The Code

Roster2 is written in a mixture of Java, Javascript, JSP and Handlebars
templates. The Java code is in src/java, the JS is in src/webapp/js and the
templates are in src/handlebars. The only place JSP is used is in
src/webapp/WEB-INF/bootstrap.jsp - that file is used to do the initial launch of
the page before Javascript and Handlebars take over. Start by having a look at
RosterTool.java - that's the Sakai hookup point. It sets some response variables
up that are then loaded into JS objects by bootstrap.jsp. After that, take a
look at roster.js. roster.js takes those objects, loads json from Sakai, and
renders Handlebars templates.

