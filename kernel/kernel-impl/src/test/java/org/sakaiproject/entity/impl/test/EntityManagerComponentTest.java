package org.sakaiproject.entity.impl.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.impl.EntityManagerComponent;
import org.sakaiproject.entity.impl.ReferenceComponent;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;


public class EntityManagerComponentTest {

	private EntityManagerComponent entityManager;
	private HashMap<String, Reference> refs;
	
    @Before
    public void setup() {
        entityManager = new EntityManagerComponent();
        entityManager.init();
        //Currently used references
        ArrayList<String> rawRefs = new ArrayList<String>(Arrays.asList("user",
        		"prefs", "site", "realm", "rubrics", "alias",
        		"calendar", "announcement", "assignment", "content",
        		"poll", "basiclti", "commons", "mailarchive", "messageforum",
        		"chat", "citation", "dropbox", "wiki", "syllabus",
        		"web", "samigo", "sitestats-report", "gradebookng"
        		));

        refs = new HashMap<String, Reference>();
        rawRefs.forEach(ref -> {
    		EntityProducer ep = mock(EntityProducer.class);
    		Reference reference = new ReferenceComponent(entityManager, ref);
    		refs.put(ref, reference);
    		when(ep.parseEntityReference(any(String.class), any(Reference.class))).thenReturn(ref.contains(reference.getReference()));
    		entityManager.registerEntityProducer(ep, ref);
        });
    }
    
    @Test
    public void getEntityProducerTest() {

    	EntityProducer epResult = entityManager.getEntityProducer("/site/dasfds", refs.get("site"));
    	assertNotNull(epResult);
    	epResult = entityManager.getEntityProducer("/assignment/a/17ddfdc0-2c79-4002-95ba-ee4307cc28ea/9902a666-d610-452b-a7ea-e6a7b0ad50cd", refs.get("assignment"));
    	assertNotNull(epResult);
    	epResult = entityManager.getEntityProducer("/announcement/channel/17ddfdc0-2c79-4002-95ba-ee4307cc28ea/main", refs.get("announcement"));
    	assertNotNull(epResult);
    	epResult = entityManager.getEntityProducer("/announcement/msg/17ddfdc0-2c79-4002-95ba-ee4307cc28ea/main/2016879d-51a8-4d8e-aa75-552537159797", refs.get("announcement"));
    	assertNotNull(epResult);
    	epResult = entityManager.getEntityProducer("/announcement/msg", refs.get("announcement"));
    	assertNotNull(epResult);
    	epResult = entityManager.getEntityProducer("announcement/msg", refs.get("announcement"));
    	assertNotNull(epResult);
    	epResult = entityManager.getEntityProducer("announcement/msg/17ddfdc0-2c79-4002-95ba-ee4307cc28ea/main/2016879d-51a8-4d8e-aa75-552537159797", refs.get("announcement"));
    	assertNotNull(epResult);
    	epResult = entityManager.getEntityProducer("site/", refs.get("site"));
    	assertNotNull(epResult);
    	epResult = entityManager.getEntityProducer("", refs.get("site"));
    	assertNull(epResult);
    	epResult = entityManager.getEntityProducer("/library", refs.get("library"));
    	assertNull(epResult);
    	epResult = entityManager.getEntityProducer("/non-valid-ref", refs.get("ref"));
    	assertNull(epResult);
    	epResult = entityManager.getEntityProducer("/wiki", refs.get("wiki"));
    	assertNotNull(epResult);
    	epResult = entityManager.getEntityProducer("/syllabus", refs.get("syllabus"));
    	assertNotNull(epResult);
    	
    }
}
