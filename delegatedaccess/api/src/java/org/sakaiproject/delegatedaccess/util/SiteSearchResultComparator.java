/*
* The Trustees of Columbia University in the City of New York
* licenses this file to you under the Educational Community License,
* Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.delegatedaccess.util;

import java.util.Comparator;

import org.sakaiproject.delegatedaccess.model.SiteSearchResult;

/**
 * 
 * Compares search results for sorting
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public class SiteSearchResultComparator implements Comparator<SiteSearchResult> {

	private int compareField = -1;

	public SiteSearchResultComparator(int compareField){
		this.compareField = compareField;
	}

	public int compare(SiteSearchResult arg0, SiteSearchResult arg1) {
		switch (compareField) {
		case DelegatedAccessConstants.SEARCH_COMPARE_SITE_ID:
			return arg0.getSiteTitle().compareTo(arg1.getSiteTitle());
		case DelegatedAccessConstants.SEARCH_COMPARE_TERM:
			return arg0.getSiteTerm().compareToIgnoreCase(arg1.getSiteTerm());
		case DelegatedAccessConstants.SEARCH_COMPARE_INSTRUCTOR:
			return arg0.getInstructorsString().compareToIgnoreCase(arg1.getInstructorsString());
		case DelegatedAccessConstants.SEARCH_COMPARE_ACCESS:
			return arg0.getAccessString().compareToIgnoreCase(arg1.getAccessString());
		case DelegatedAccessConstants.SEARCH_COMPARE_START_DATE:
			if(arg0.getShoppingPeriodStartDate() == null && arg1.getShoppingPeriodStartDate() == null){
				return 0;
			}else if(arg0.getShoppingPeriodStartDate() == null){
				return 1;
			}else if(arg1.getShoppingPeriodStartDate() == null){
				return -1;
			}else{
				return arg0.getShoppingPeriodStartDate().compareTo(arg1.getShoppingPeriodStartDate());
			}
		case DelegatedAccessConstants.SEARCH_COMPARE_END_DATE:
			if(arg0.getShoppingPeriodEndDate() == null && arg1.getShoppingPeriodEndDate() == null){
				return 0;
			}else if(arg0.getShoppingPeriodEndDate() == null){
				return 1;
			}else if(arg1.getShoppingPeriodEndDate() == null){
				return -1;
			}else{
				return arg0.getShoppingPeriodEndDate().compareTo(arg1.getShoppingPeriodEndDate());
			}
		case DelegatedAccessConstants.SEARCH_COMPARE_ACCESS_MODIFIED:
			if(arg0.getModified() == null && arg1.getModified() == null){
				return 0;
			}else if(arg0.getModified() == null){
				return 1;
			}else if(arg1.getModified() == null){
				return -1;
			}else{
				return arg0.getModified().compareTo(arg1.getModified());
			}
		case DelegatedAccessConstants.SEARCH_COMPARE_ACCESS_MODIFIED_BY:
			return arg0.getModifiedBySortName().compareToIgnoreCase(arg1.getModifiedBySortName());
		case DelegatedAccessConstants.SEARCH_COMPARE_PUBLISHED:
			if(arg0.isSitePublished() && arg1.isSitePublished()){
				return 0;
			}else if(!arg0.isSitePublished() && !arg1.isSitePublished()){
				return 0;
			}else if(arg0.isSitePublished()){
				return 1;
			}
		case DelegatedAccessConstants.SEARCH_COMPARE_PROVIDERS:
			return arg0.getProviders().compareToIgnoreCase(arg1.getProviders());
		case DelegatedAccessConstants.SEARCH_COMPARE_SITE_TITLE:
		default:
			return arg0.getSiteTitle().compareToIgnoreCase(arg1.getSiteTitle());
		}
	}

}
