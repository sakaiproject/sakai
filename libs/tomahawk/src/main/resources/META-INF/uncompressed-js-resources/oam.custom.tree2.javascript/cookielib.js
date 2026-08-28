//=============================================================================
// CookieLib Definition
// Contains general purpose javascript methods for managing html cookies
//=============================================================================

function CookieLib(){}

CookieLib.COOKIE_DELIM = ";";
CookieLib.COOKIE_KEYVAL = "=";
CookieLib.ATTRIB_DELIM = ";";
CookieLib.ATTRIB_KEYVAL = "=";

/**
 * Retrieves the specified cookie as a String of text
 * @param String name - name of cookie to retrieve
 * @return String cookie value or null if not found
 */
function CookieLib_getRawCookie(name) {
    var search = name + CookieLib.COOKIE_KEYVAL;
    if (document.cookie)
    {
        if (document.cookie.length > 0)
        {
            offset = document.cookie.indexOf(search);
            if (offset != -1)
            {
               offset += search.length;
               end = document.cookie.indexOf(CookieLib.COOKIE_DELIM, offset);
               if (end == -1) end = document.cookie.length;
               return unescape(document.cookie.substring(offset, end));
            }
        }
    }
    return null;
}
CookieLib.getRawCookie = CookieLib_getRawCookie;

/**
 * Cookies can hold multiple pieces of information.  This methods saves a key/value pair to
 * the specified cookie.  Each key/value pair is separated by a special character
 * defined by the ATTRIB_DELIM constant.  Setting an attribute's value to null or empty string
 * will remove it from the cookie.
 * @param cookieName String - name of cookie that will hold the key/value pair
 * @param attribName String - attribute key
 * @param attribValue String - attribute value
 */
function CookieLib_setCookieAttrib(cookieName, attribName, attribValue)
{
    var attribMap = CookieLib.getCookie(cookieName);
    attribMap[attribName] = attribValue;
    CookieLib.setCookie(cookieName,attribMap);
}
CookieLib.setCookieAttrib = CookieLib_setCookieAttrib;

/**
 * Cookies can hold multiple pieces of information.  This methods retrieves a value from the
 * specified cookie using the specified key (attribName).  Each key/value pair is separated by a
 * special character defined by the ATTRIB_DELIM constant.
 * @param cookieName String - name of cookie that that holds the key/value pair
 * @param attribName String - attribute key
 * @param attribValue String - attribute value
 * @return String value
 */
function CookieLib_getCookieAttrib(cookieName, attribName)
{
    var attribMap = CookieLib.getCookie(cookieName);
    return attribMap[attribName];
}
CookieLib.getCookieAttrib = CookieLib_getCookieAttrib;

/**
 * Retrieves a map of all key/value pairs (attributes) stored in the specified cookie.
 * @param cookieName String - name of cookie
 * @return Array of all attributes
 */
function CookieLib_getCookie(cookieName)
{
    var attribMap = new Array();
    var cookie = CookieLib.getRawCookie(cookieName);
    if (typeof( cookie ) != "undefined"  && cookie != null)
    {
        var attribArray = cookie.split(CookieLib.ATTRIB_DELIM);
        for (var i=0;i<attribArray.length;i++)
        {
            var index = attribArray[i].indexOf(CookieLib.ATTRIB_KEYVAL);
            var name =  attribArray[i].substring(0,index);
            var value = attribArray[i].substring(index+1);
            attribMap[name] = value;
        }
    }
    return attribMap;
}
CookieLib.getCookie = CookieLib_getCookie;

/**
 * Saves a map of cookie attributes to the specified cookie.  Null or empty string values are not saved.
 * @param cookieName String - name of cookie to create
 * @param attribMap Array - holds key/value pairs to save in cookie
 */
function CookieLib_setCookie(cookieName, attribMap)
{
    var attrib = "";
    for (var name in attribMap)
    {
        var value = attribMap[name];

        if (typeof( value ) != "undefined"  && value != null && value != "" && typeof(value) != "function")
        {
            if (name.indexOf(CookieLib.ATTRIB_KEYVAL) < 0 && value.indexOf(CookieLib.ATTRIB_KEYVAL) < 0 &&
                name.indexOf(CookieLib.ATTRIB_DELIM) < 0 && value.indexOf(CookieLib.ATTRIB_DELIM) < 0)
            {
                attrib += ((attrib == "") ? "" : CookieLib.ATTRIB_DELIM);
                attrib += (name + CookieLib.ATTRIB_KEYVAL + value);
             }
            else
            {
                alert("Cookie attribute name and/or value contains a delimeter (" +
                       CookieLib.ATTRIB_KEYVAL + " or " + CookieLib.ATTRIB_DELIM + ").");
            }
        }
    }
    document.cookie = cookieName + CookieLib.COOKIE_KEYVAL + escape(attrib);
}
CookieLib.setCookie = CookieLib_setCookie;
