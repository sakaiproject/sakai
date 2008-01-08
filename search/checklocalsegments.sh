#!/bin/sh
java -classpath search-impl/impl/target/sakai-search-impl-SNAPSHOT.jar org.sakaiproject.search.journal.impl.SegmentListStore $1

