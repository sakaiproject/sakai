package org.sakaiproject.lti.api;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 2/15/12
 * Time: 5:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class LTIException extends Exception {
    private String errorKey;

    public LTIException(String errorKey, String errorMessage) {
        super(errorMessage);
        this.errorKey = errorKey;
    }


    public LTIException(String errorKey, String errorMessage, Exception exception) {
        super(errorMessage, exception);
        this.errorKey = errorKey;
    }

    public String getErrorKey() {
        return errorKey;
    }
}
