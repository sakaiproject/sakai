package org.sakaiproject.assignment.impl;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;

import java.util.List;
import java.util.ArrayList;

/**
 * A simple SecurityAdviser that can be used to override permissions on one reference string for
 * one user for one function.
 */
public class MySecurityAdvisor implements SecurityAdvisor {
    
    protected String m_userId;
    
    protected List<String> m_functions = new ArrayList<String>();
    
    protected List<String> m_references = new ArrayList<String>();
    
    public MySecurityAdvisor(String userId, String function, String reference) {
        m_userId = userId;
        m_functions.add(function);
        if (reference != null && !reference.isEmpty())
        	m_references.add(reference);
    }
    
    public MySecurityAdvisor(String userId, String function, List<String> references) {
        m_userId = userId;
        m_functions.add(function);
        if (references != null && !references.isEmpty())
        	m_references = references;
    }
    
    public MySecurityAdvisor(String userId, List<String> functions, String reference) {
        m_userId = userId;
        m_functions = functions;
        if (reference != null && !reference.isEmpty())
        	m_references.add(reference);
    }
    
    public MySecurityAdvisor(String userId, List<String> functions, List<String> references) {
        m_userId = userId;
        m_functions = functions;
        if (references != null && !references.isEmpty())
        	m_references = references;
    }
    
    public SecurityAdvice isAllowed(String userId, String function, String reference) {
        SecurityAdvice rv = SecurityAdvice.PASS;
        if (m_userId.equals(userId) && m_functions.contains(function)
            && (m_references.isEmpty() || m_references.contains(reference))) {
            rv = SecurityAdvice.ALLOWED;
        }
        return rv;
    }
}