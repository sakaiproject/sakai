/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.component.gradebook;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Generic helper class for serializing Java objects to and from simply-formatted XML
 * for archival and reconstitution across data definition versions.
 * <p>
 * XStream is used to handle the marshalling and unmarshalling work. The main
 * addition is an "externalizableVersion" attribute on the POJO's top-level
 * element. That attribute can then be checked for incompatibilities before
 * reconstitution, and used to convert old data into its new form. (Currently,
 * if there's a version mismatch and nothing is done about it, this class throws
 * a ConversionException.)
 * <p>
 * Translation to and from XML can be handled either with the static "toXML"
 * and "fromXML" methods, or through the Externalizable interface. The chief
 * benefit of the static methods is that they (theoretically) give subclasses
 * the ability to translate across versions using XSLT, and possibly even return
 * an object of a different class than the original.
 * <p>
 * TODO For the functionality being checked in (site-to-site migration), this class
 * is not strictly necessary. It's here on a speculative basis for upcoming 
 * import/archive/merge development. 
 * 
 * @deprecated This is part of the import/export for gradebook1 which will be removed at some point
 */
@Deprecated
public abstract class VersionedExternalizable implements Externalizable {
	public static String VERSION_ATTRIBUTE = "externalizableVersion";
	
	/**
	 * @return non-null archivable version identifier for the object definition
	 */
	public abstract String getExternalizableVersion();
	
    /**
     * This XStream converter stores the externalizable version of the
     * class as a Document-level attribute for easy access by translators.
     * Right now, though, since we don't have any version translators, we
     * don't try to reconstitute XML corresponding to anything but the current
     * version.
     */
	public static class Converter extends AbstractReflectionConverter {
     	public Converter(Mapper mapper, ReflectionProvider reflectionProvider) {
    		super(mapper, reflectionProvider);
    	}
        public boolean canConvert(Class type) {
            return VersionedExternalizable.class.isAssignableFrom(type);
        }
        public void marshal(Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        	writer.addAttribute(VERSION_ATTRIBUTE, ((VersionedExternalizable)source).getExternalizableVersion());
        	super.marshal(source, writer, context);
        }
        public Object doUnmarshal(Object result, HierarchicalStreamReader reader, UnmarshallingContext context) {
        	String currentVersion = ((VersionedExternalizable)result).getExternalizableVersion();
        	String oldVersion = reader.getAttribute(VERSION_ATTRIBUTE);
        	if ((oldVersion == null) || !currentVersion.equals(oldVersion)) {
    			// This is one place we might put a version translation method in the future....
        		throw new ConversionException("Cannot convert " + result + " from version " + oldVersion + " to version " + currentVersion);       		
        	}
        	return super.doUnmarshal(result, reader, context);
        }
    }
	
	protected static XStream getXStream() {
    	XStream xstream = new XStream(new DomDriver());	// does not require XPP3 library
    	xstream.registerConverter(new Converter(xstream.getMapper(), xstream.getReflectionProvider()));
    	return xstream;
	}

	public void readExternal(ObjectInput inputStream) throws IOException, ClassNotFoundException {
		getXStream().fromXML(inputStream.readUTF(), this);
	}

	public void writeExternal(ObjectOutput outputStream) throws IOException {
		outputStream.writeUTF(getXStream().toXML(this));
	}
	
	/**
	 * @param obj the Java object (usually a subclass of VersionedExternalizable) to describe
	 * as XML
	 * @return XML describing the object
	 */
	public static String toXml(Object obj) {
		return getXStream().toXML(obj);
	}
	
	/**
	 * @param xmlString XML string (presumably created by this class) describing a Java object
	 * @return the Java object it describes
	 */
	public static Object fromXml(String xmlString) {
		return getXStream().fromXML(xmlString);
	}

}
