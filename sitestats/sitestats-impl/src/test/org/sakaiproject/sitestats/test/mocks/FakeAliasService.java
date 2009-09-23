/**
 * $URL:$
 * $Id:$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.test.mocks;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.sakaiproject.alias.api.AliasEdit;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FakeAliasService implements AliasService {

	public AliasEdit add(String arg0) throws IdInvalidException, IdUsedException, PermissionException {
		// TODO Auto-generated method stub
		return null;
	}

	public String aliasReference(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean allowAdd() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean allowEdit(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean allowRemoveAlias(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean allowRemoveTargetAliases(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean allowSetAlias(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public void cancel(AliasEdit arg0) {
		// TODO Auto-generated method stub

	}

	public void commit(AliasEdit arg0) {
		// TODO Auto-generated method stub

	}

	public int countAliases() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int countSearchAliases(String arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public AliasEdit edit(String arg0) throws IdUnusedException, PermissionException, InUseException {
		// TODO Auto-generated method stub
		return null;
	}

	public List getAliases(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public List getAliases(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public List getAliases(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTarget(String alias) throws IdUnusedException {
		final String aliasSuffix = "-alias";
		if(alias.endsWith(aliasSuffix)) {
			return "/site/" + alias.substring(0, alias.indexOf(aliasSuffix)); 
		}
		return null;
	}

	public void remove(AliasEdit arg0) throws PermissionException {
		// TODO Auto-generated method stub

	}

	public void removeAlias(String arg0) throws IdUnusedException, PermissionException, InUseException {
		// TODO Auto-generated method stub

	}

	public void removeTargetAliases(String arg0) throws PermissionException {
		// TODO Auto-generated method stub

	}

	public List searchAliases(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setAlias(String arg0, String arg1) throws IdUsedException, IdInvalidException, PermissionException {
		// TODO Auto-generated method stub

	}

	public String archive(String arg0, Document arg1, Stack arg2, String arg3, List arg4) {
		// TODO Auto-generated method stub
		return null;
	}

	public Entity getEntity(Reference arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection getEntityAuthzGroups(Reference arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityDescription(Reference arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ResourceProperties getEntityResourceProperties(Reference arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityUrl(Reference arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public HttpAccess getHttpAccess() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	public String merge(String arg0, Element arg1, String arg2, String arg3, Map arg4, Map arg5, Set arg6) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean parseEntityReference(String arg0, Reference arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean willArchiveMerge() {
		// TODO Auto-generated method stub
		return false;
	}

}
