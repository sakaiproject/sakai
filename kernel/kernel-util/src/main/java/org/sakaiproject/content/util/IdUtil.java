package org.sakaiproject.content.util;

public class IdUtil {

    /**
     * Find the containing collection id of a given resource id.
     *
     * @param id
     *        The resource id.
     * @return the containing collection id.
     */
    public static String isolateContainingId(String id)
    {
        // take up to including the last resource path separator, not counting one at the very end if there
        return id.substring(0, id.lastIndexOf('/', id.length() - 2) + 1);

    } // isolateContainingId

    /**
     * Find the resource name of a given resource id or filepath.
     *
     * @param id
     *        The resource id.
     * @return the resource name.
     */
    public static String isolateName(String id)
    {
        if (id == null) {return null;}
        if (id.length() == 0) {return null;}

        // take after the last resource path separator, not counting one at the very end if there
        boolean lastIsSeparator = id.charAt(id.length() - 1) == '/';
        return id.substring(id.lastIndexOf('/', id.length() - 2) + 1, (lastIsSeparator ? id.length() - 1 : id.length()));

    } // isolateName
}
