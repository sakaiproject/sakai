package org.tsugi.lti;

/**
 * Utility class for normalizing XML strings for comparison in unit tests.
 * 
 * This class provides methods to normalize XML formatting differences
 * (whitespace, XML declarations, etc.) so that XML strings can be compared
 * semantically rather than syntactically.
 */
public class XmlNormalizationUtil {
    
    /**
     * Normalizes XML for comparison by:
     * - Removing XML declaration
     * - Removing empty namespace declarations (xmlns="")
     * - Removing whitespace between tags
     * - Normalizing remaining whitespace to single spaces
     * - Trimming leading/trailing whitespace
     * 
     * This is useful for comparing XML strings that may have different
     * formatting but the same semantic content. Empty namespace declarations
     * are removed because Jackson may inject xmlns="" attributes that don't
     * affect semantic equality.
     * 
     * @param xml The XML string to normalize
     * @return The normalized XML string
     */
    public static String normalizeForComparison(String xml) {
        if (xml == null) {
            return null;
        }
        
        // Remove XML declaration
        xml = xml.replaceAll("^<\\?xml[^>]*>\\s*", "");
        
        // Remove empty namespace declarations (xmlns="")
        // Jackson may inject these but they don't affect semantic equality
        xml = xml.replaceAll("\\s+xmlns=\"\"", "");
        
        // Remove whitespace between tags (including newlines)
        xml = xml.replaceAll(">\\s+<", "><");
        
        // Normalize remaining whitespace to single spaces
        xml = xml.replaceAll("\\s+", " ");
        
        // Trim leading/trailing whitespace
        xml = xml.trim();
        
        return xml;
    }
    
    /**
     * Normalizes whitespace in XML by removing whitespace between tags and trimming.
     * This is a simpler normalization that doesn't remove XML declarations or normalize
     * internal whitespace - useful for Jackson-generated XML that may have xmlns="" attributes.
     * 
     * @param xml The XML string to normalize
     * @return The normalized XML string with whitespace between tags removed
     */
    public static String normalizeWhitespace(String xml) {
        if (xml == null) {
            return null;
        }
        
        // Remove whitespace between tags
        xml = xml.replaceAll(">\\s+<", "><");
        
        // Trim leading/trailing whitespace
        xml = xml.trim();
        
        return xml;
    }
}
