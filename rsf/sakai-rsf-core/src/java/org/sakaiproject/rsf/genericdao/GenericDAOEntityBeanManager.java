/**
 * Copyright © 2005, CARET, University of Cambridge
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
/*
 * Created on Dec 3, 2006
 */
package org.sakaiproject.rsf.genericdao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.genericdao.api.CoreGenericDao;
import org.sakaiproject.genericdao.api.InitializingCoreGenericDAO;

import uk.org.ponder.beanutil.FallbackBeanLocator;
import uk.org.ponder.rsf.state.entity.DefaultEntityMapper;
import uk.org.ponder.rsf.state.entity.EntityNameInferrer;
import uk.org.ponder.rsf.state.entity.support.BasicObstinateEBL;
import uk.org.ponder.saxalizer.AccessMethod;
import uk.org.ponder.saxalizer.support.MethodAnalyser;
import uk.org.ponder.saxalizer.SAXalizerMappingContext;
import uk.org.ponder.util.ObjectFactory;

public class GenericDAOEntityBeanManager implements FallbackBeanLocator, EntityNameInferrer {
	private Map locators = new HashMap();

	private CoreGenericDao genericDAO;
	private SAXalizerMappingContext mappingcontext;

	public void setGenericDAO(CoreGenericDao genericDAO) {
		this.genericDAO = genericDAO;
	}

	public void setMappingContext(SAXalizerMappingContext mappingcontext) {
		this.mappingcontext = mappingcontext;
	}

	public String getEntityName(Class entityclazz) {
		// NB, this is the H2 implementation, until GenericDAO is upgraded to
		// use
		// entity names.
		String classname = entityclazz.getName();
		int lastdotpos = classname.lastIndexOf('.');
		String nick = classname.substring(lastdotpos + 1);
		return nick;
	}

	public void init() {
		List classes = genericDAO.getPersistentClasses();
		for (int i = 0; i < classes.size(); ++i) {
			Class clazz = (Class) classes.get(i);
			String idprop = genericDAO.getIdProperty(clazz);
			MethodAnalyser ma = mappingcontext.getAnalyser(clazz);
			AccessMethod am = ma.getAccessMethod(idprop);
			Class idclazz = am.getDeclaredType();

			DefaultEntityMapper dem = new DefaultEntityMapper();
			dem.setEntityClass(clazz);
			dem.setIDClass(idclazz);
			dem.setMappingContext(mappingcontext);
			if (genericDAO instanceof InitializingCoreGenericDAO) {
				dem.setObjectFactory(new ObjectFactory() {
					public Object getObject() {
						return ((InitializingCoreGenericDAO) genericDAO).instantiate();
					}
				});
			}

			GenericDAOEntityHandler gdeh = new GenericDAOEntityHandler();
			gdeh.setGenericDAO(genericDAO);
			gdeh.setPersistentClass(clazz);
			BasicObstinateEBL boebl = new BasicObstinateEBL();
			boebl.setEntityHandler(gdeh);
			boebl.setEntityMapper(dem);
			String entityname = getEntityName(clazz);
			locators.put(entityname, boebl);
		}
	}

	public Object locateBean(String path) {
		return locators.get(path);
	}

}
