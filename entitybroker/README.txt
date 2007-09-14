This is the Entity Broker project - an enhancement to the way entities work inside Sakai

This contains information related to the Entity Broker system (entity 2.0) in Sakai. 
Ideally we want flexible entities which make development of integrated Sakai tools 
easier and more flexible. Our goals are to make the entity system easier on developers 
(easier to use and understnad) and more powerful (easier to extend and improve).

Functional prototype code: https://source.sakaiproject.org/contrib/caret/entitybroker/
Test application: https://source.sakaiproject.org/contrib/caret/entitybroker-test/

Usage:
Simply checkout this project in your sakai source root and run "maven sakai" to
build the project. Then take a look at the APIs (in particular EntityProvider and
CoreEntityProvider) and implement at least one provider and as many capabilities as
you like (instructions are in the interface comments).

More info here:
http://confluence.sakaiproject.org/confluence/x/Sac

Comments or questions about the entity broker should go to 
Aaron Zeckoski (aaronz@vt.edu) or Antranig Basman (antranig@caret.cam.ac.uk)