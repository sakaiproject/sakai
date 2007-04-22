/*
 ***************************************************************************
 * Copyright 2003-2005 Luca Passani, passani at eunet.no                   *
 * Distributed under the Mozilla Public License                            *
 *   http://www.mozilla.org/NPL/MPL-1.1.txt                                *
 ***************************************************************************
 *   $Author$
 *   $Header: /cvsroot/wurfl/tools/java/wurflapi-xom/antbuild/src/net/sourceforge/wurfl/wurflapi/WurflException.java,v 1.2 2005/02/13 15:11:39 passani Exp $
 */

package net.sourceforge.wurfl.wurflapi;

class WurflException extends RuntimeException {

    public WurflException() {}

    public WurflException(String msg) { super(msg); }

}
