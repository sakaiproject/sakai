/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaibrary.xserver.session;

public class MetasearchSession implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private String guid;
	
  private String username;
  private String password;
	private boolean isLoggedIn;
	private String sessionId;
  private String baseUrl;
	
	private org.osid.shared.Id repositoryId;
	private String repositoryDisplayName;

	private org.osid.shared.Properties searchProperties;
  private java.util.Properties searchStatusProperties;
  private boolean singleSearchSource;
  private boolean gotMergeError;
	
	private String foundGroupNumber;
	private String mergedGroupNumber;
	private String recordsSetNumber;
	
	private Integer numRecordsFound;
	private Integer numRecordsFetched;
	private Integer numRecordsMerged;
	
	public MetasearchSession( String guid ) {
		this.guid = guid;
	}
	
	public String getGuid() {
		return guid;
	}
	
	public String getFoundGroupNumber() {
		return foundGroupNumber;
	}
	
	public void setFoundGroupNumber(String foundGroupNumber) {
		this.foundGroupNumber = foundGroupNumber;
	}
	
	public boolean isLoggedIn() {
		return isLoggedIn;
	}
	
	public void setLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}
	
	public String getRecordsSetNumber() {
		return recordsSetNumber;
	}
	
	public void setRecordsSetNumber(String recordsSetNumber) {
		this.recordsSetNumber = recordsSetNumber;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Integer getNumRecordsFound() {
		return numRecordsFound;
	}

	public void setNumRecordsFound(Integer numRecordsFound) {
		this.numRecordsFound = numRecordsFound;
	}

	public Integer getNumRecordsFetched() {
		return numRecordsFetched;
	}

	public void setNumRecordsFetched(Integer numRecordsFetched) {
		this.numRecordsFetched = numRecordsFetched;
	}

	public String getMergedGroupNumber() {
		return mergedGroupNumber;
	}

	public void setMergedGroupNumber(String mergedGroupNumber) {
		this.mergedGroupNumber = mergedGroupNumber;
	}

	public org.osid.shared.Properties getSearchProperties() {
		return searchProperties;
	}

	public void setSearchProperties(org.osid.shared.Properties searchProperties) {
		this.searchProperties = searchProperties;
	}

	public org.osid.shared.Id getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(org.osid.shared.Id repositoryId) {
		this.repositoryId = repositoryId;
	}

	public String getRepositoryDisplayName() {
		return repositoryDisplayName;
	}

	public void setRepositoryDisplayName(String repositoryDisplayName) {
		this.repositoryDisplayName = repositoryDisplayName;
	}

	public Integer getNumRecordsMerged() {
		return numRecordsMerged;
	}

	public void setNumRecordsMerged(Integer numRecordsMerged) {
		this.numRecordsMerged = numRecordsMerged;
	}

  public boolean isSingleSearchSource() {
    return singleSearchSource;
  }

  public void setSingleSearchSource(boolean singleSearchSource) {
    this.singleSearchSource = singleSearchSource;
  }

  public boolean isGotMergeError() {
    return gotMergeError;
  }

  public void setGotMergeError(boolean gotMergeError) {
    this.gotMergeError = gotMergeError;
  }

  public java.util.Properties getSearchStatusProperties() {
    return searchStatusProperties;
  }

  public void setSearchStatusProperties(
      java.util.Properties searchStatusProperties) {
    this.searchStatusProperties = searchStatusProperties;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
