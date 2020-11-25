package org.sakaiproject.rubrics.api.test;

import org.sakaiproject.rubrics.logic.AuthenticatedRequestContext;

public class SpelContext {

    public AuthenticatedRequestContext principal;

    public static SpelContext from(AuthenticatedRequestContext principal) {

        SpelContext context = new SpelContext();
        context.principal = principal;
        return context;
    }
}
