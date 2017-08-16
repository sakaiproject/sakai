package org.sakaiproject.webservices.interceptor;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Simple marker annotation to say that a REST request shouldn't have IP restrictions on it and should bypass
 * any filter restrictions.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoIPRestriction {
}
