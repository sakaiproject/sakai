package org.sakaiproject.gradebookng.business;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.sakaiproject.gradebookng.business.dto.AssignmentOrder;
import org.sakaiproject.gradebookng.business.dto.GradebookUserPreferences;
import org.sakaiproject.gradebookng.business.util.XmlList;

/**
 * Handles conversion of objects to and from XML
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * 
 */
public class XmlMarshaller {

	private static Marshaller marshaller = null;
    private static Unmarshaller unmarshaller = null;

    static {
        try {
        	//ensure the full set of classes are added to this list
            JAXBContext context = JAXBContext.newInstance(
            		GradebookUserPreferences.class,
            		AssignmentOrder.class,
            		XmlList.class);
            marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            unmarshaller = context.createUnmarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException("Couldn't create JAXB context", e);
        }
    }

	/**
	 * Convert an object to an XML string
	 * 
	 * @param object the object to serialise
	 * @return
	 * @throws JAXBException
	 */
    public static String marshal(Object object) throws JAXBException {
		final StringWriter writer = new StringWriter();
		marshaller.marshal(object, writer);		
		return writer.toString();
    }

	/**
	 * Convert this XML back into object form
	 * @param xml the XML string to convert
	 * @return
	 * @throws JAXBException
	 */
	public static <T> Object unmarshall(String xml) throws JAXBException {
		return unmarshaller.unmarshal(new StringReader(xml));
	}

}
