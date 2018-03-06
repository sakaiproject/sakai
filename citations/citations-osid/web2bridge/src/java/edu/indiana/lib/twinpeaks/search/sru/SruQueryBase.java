/**********************************************************************************
*
 * Copyright (c) 2003, 2004, 2008 The Sakai Foundation
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
package edu.indiana.lib.twinpeaks.search.sru;

import lombok.extern.slf4j.Slf4j;

import edu.indiana.lib.twinpeaks.search.HttpTransactionQueryBase;
import edu.indiana.lib.twinpeaks.util.StringUtils;

/**
 * Basic SRU query functionality
 */
@Slf4j
public abstract class SruQueryBase extends HttpTransactionQueryBase
{
  /*
   * SRU common parameters
   */
  public static final String SRU_VERSION          = "version";

  public static final String SRU_OPERATION        = "operation";
  public static final String SRU_EXPLAIN          = "explain";
  public static final String SRU_SEARCH_RETRIEVE  = "searchRetrieve";
  public static final String SRU_STATUS           = "status";

  public static final String SRU_RECORD_PACKING   = "recordPacking";
  public static final String SRU_RECORD_SCHEMA    = "recordSchema";

  public static final String SRU_START_RECORD     = "startRecord";
  public static final String SRU_MAX_RECORD       = "maximumRecords";

  public static final String SRU_SORT             = "sortKeys";
  public static final String SRU_QUERY            = "query";

  /*
   * GET helpers
   */

  /**
   * Make a version parameter (this is the version of our SRU request)
   * @return The fully formed version parameter
   */
  protected String sruVersion(String version)
  {
    return formatParameter(SRU_VERSION, version);
  }

  /**
   * Make an explain operation parameter
   * @return The fully formed operation
   */
  protected String sruExplain()
  {
    return sruOperation(SRU_EXPLAIN);
  }

  /**
   * Make a searchRetrieve operation parameter
   * @return The fully formed operation
   */
  protected String sruSearchRetrieve()
  {
    return sruOperation(SRU_SEARCH_RETRIEVE);
  }

  /**
   * Make an SRU operation parameter
   * @param The desired operation (<i>searchRetrieve</>, <i>explain</i>, etc)
   * @return The fully formed operation
   */
  protected String sruOperation(String operation)
  {
    return formatParameter(SRU_OPERATION, operation);
  }

  /**
   * Make a status operation parameter
   * @return The fully formed operation
   */
  protected String sruStatus()
  {
    return sruOperation(SRU_STATUS);
  }

  /**
   * Make a record packing parameter
   * @param packing How to pack (escape) result records (typically <i>xml</i>)
   * @return A fully formed record packing parameter
   */
  protected String sruRecordPacking(String packing)
  {
    return formatParameter(SRU_RECORD_PACKING, packing);
  }

  /**
   * Make a record schema parameter
   * @param schema Schema to use
   * @return A fully formed schema parameter
   */
  protected String sruRecordSchema(String schema)
  {
    return formatParameter(SRU_RECORD_SCHEMA, schema);
  }

  /**
   * Make a start record parameter
   * @param start The starting record number
   * @return A fully formed start record parameter
   */
  protected String sruStartRecord(String start)
  {
    return formatParameter(SRU_START_RECORD, start);
  }

  /**
   * Make a start record parameter
   * @param start The starting record number
   * @return A fully formed start record parameter
   */
  protected String sruStartRecord(int start)
  {
    return sruStartRecord(String.valueOf(start));
  }

  /**
   * Make a maximum record parameter
   * @param maximum The maximum record to return
   * @return A fully formed maximum record parameter
   */
  protected String sruMaximumRecords(int maximum)
  {
    return sruMaximumRecords(String.valueOf(maximum));
  }

  /**
   * Make a maximum record parameter
   * @param maximum The maximum record to return
   * @return A fully formed maximum record parameter
   */
  protected String sruMaximumRecords(String maximum)
  {
    return formatParameter(SRU_MAX_RECORD, maximum);
  }

  /**
   * Make a sort parameter
   * @param key The sort key
   * @return A fully formed sort parameter
   */
  protected String sruSort(String key)
  {
    return formatParameter(SRU_SORT, key);
  }

  /**
   * Make a query parameter
   * @param criteria The search criteria
   * @return A fully formed query parameter
   */
  protected String sruQuery(String criteria)
  {
    return formatParameter(SRU_QUERY, criteria);
  }

  /*
   * POST helpers
   */

  /**
   * Make a version parameter (this is the version of our SRU request)
   */
  protected void sruPostVersion(String version)
  {
    setParameter(SRU_VERSION, version);
  }

  /**
   * Make an explain operation parameter
   */
  protected void sruPostExplain()
  {
    sruPostOperation(SRU_EXPLAIN);
  }

  /**
   * Make a searchRetrieve operation parameter
   */
  protected void sruPostSearchRetrieve()
  {
    sruPostOperation(SRU_SEARCH_RETRIEVE);
  }

  /**
   * Make an SRU operation parameter
   * @param The desired operation (<i>searchRetrieve</>, <i>explain</i>, etc)
   */
  protected void sruPostOperation(String operation)
  {
    setParameter(SRU_OPERATION, operation);
  }

  /**
   * Make a status operation parameter
   */
  protected void sruPostStatus()
  {
    sruPostOperation(SRU_STATUS);
  }

  /**
   * Make a record packing parameter
   * @param packing How to pack (escape) result records (typically <i>xml</i>)
   */
  protected void sruPostRecordPacking(String packing)
  {
    setParameter(SRU_RECORD_PACKING, packing);
  }

  /**
   * Make a record schema parameter
   * @param schema Schema to use
   */
  protected void sruPostRecordSchema(String schema)
  {
    setParameter(SRU_RECORD_SCHEMA, schema);
  }

  /**
   * Make a start record parameter
   * @param start The starting record number
   */
  protected void sruPostStartRecord(String start)
  {
    setParameter(SRU_START_RECORD, start);
  }

  /**
   * Make a start record parameter
   * @param start The starting record number
   */
  protected void sruPostStartRecord(int start)
  {
    sruPostStartRecord(String.valueOf(start));
  }

  /**
   * Make a maximum record parameter
   * @param maximum The maximum record to return
   */
  protected void sruPostMaximumRecords(int maximum)
  {
    sruPostMaximumRecords(String.valueOf(maximum));
  }

  /**
   * Make a maximum record parameter
   * @param maximum The maximum record to return
   */
  protected void sruPostMaximumRecords(String maximum)
  {
    setParameter(SRU_MAX_RECORD, maximum);
  }

  /**
   * Make a sort parameter
   * @param key The sort key
   */
  protected void sruPostSort(String key)
  {
    setParameter(SRU_SORT, key);
  }

  /**
   * Make a query parameter
   * @param criteria The search criteria
   */
  protected void sruPostQuery(String criteria)
  {
    setParameter(SRU_QUERY, criteria);
  }

  /*
   * Miscellaneous helpers
   */

  /**
   * Trim and concatenate all arguments
   * @param items Items to append
   * @return The concatenated items
   */
  protected String appendItems(String... items)
  {
    StringBuilder itemBuffer = new StringBuilder();

    for (String item: items)
    {
      itemBuffer.append(item.trim());
    }
    return itemBuffer.toString();
  }

  /**
   * Format a parameter (<code>name=value</code>)
   * @param name Parameter name
   * @param value Parameter value
   * @return The <code>name=value</code> pair
   */
  protected String formatParameter(String name, String value)
  {
    return appendItems(name, "=", normalizeParameter(value));
  }

  /**
   * Add the first parameter to a new parameter list
   * @param newParameter Parmeter to add
   * @return the updated list
   */
  protected String addFirstParameter(String newParameter)
  {
    return addParameter(null, newParameter);
  }

  /**
   * Add a parameter to the parameter list
   * @param base Base parameter list - we'll add to this
   * @param newParameter Parmeter to add
   * @return the updated list
   */
  protected String addParameter(String base, String newParameter)
  {
    String seperator;

    if (StringUtils.isNull(base))
    {
      return appendItems("?", newParameter);
    }

    seperator = "&";
    if (base.indexOf("?") == -1)
    {
      seperator = "?";
    }

    return appendItems(base, seperator, newParameter);
  }

  /**
   * Normalize a query parameter value
   * @param value Parameter value
   * @return The [possibly] normalized value
   */
  protected String normalizeParameter(String value)
  {
    if (value == null)
    {
      return "";
    }
    return value.trim();
  }
}