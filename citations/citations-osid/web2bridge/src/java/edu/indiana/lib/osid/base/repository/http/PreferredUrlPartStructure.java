package edu.indiana.lib.osid.base.repository.http;

/*******************************************************************************
 * Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan,
 * Trustees of Indiana University, Board of Trustees of the Leland Stanford,
 * Jr., University, and The MIT Corporation
 *
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you
 * have read, understand, and will comply with the terms and conditions of the
 * Educational Community License.  You may obtain a copy of the License at:
 *
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
*******************************************************************************/

public class PreferredUrlPartStructure implements org.osid.repository.PartStructure
{
	private static org.apache.commons.logging.Log	_log = edu.indiana.lib.twinpeaks.util.LogUtils.getLog(PreferredUrlPartStructure.class);

  private org.osid.shared.Id PREFERREDURL_PART_STRUCTURE_ID = null;

  private org.osid.shared.Type type = new Type( "sakaibrary", "partStructure",
  		                                          "preferredUrl", "PreferredURL" );
  private String displayName = "PreferredURL";
  private String description = "The preferred URL should be persistent, pointing "
                             + "to the same resource over time";
  private boolean mandatory = false;
  private boolean populatedByRepository = false;
  private boolean repeatable = false;

	private static PreferredUrlPartStructure preferredUrlPartStructure;

  private PreferredUrlPartStructure()
  {
    try
    {
      PREFERREDURL_PART_STRUCTURE_ID = Managers.getIdManager().getId("2c7464123410800d6d751h2016821340t");
    }
    catch (Throwable t)
    {
    	throw new RuntimeException(t.toString());
    }
  }

  protected static synchronized PreferredUrlPartStructure getInstance()
	{
		if (preferredUrlPartStructure == null)
	  {
		  preferredUrlPartStructure = new PreferredUrlPartStructure();
		}
		return preferredUrlPartStructure;
	}

	/**
	 * Public method to fetch the PartStructure ID
	 */
	public static org.osid.shared.Id getPartStructureId()
	{
		org.osid.shared.Id id = null;

		try
		{
			id = getInstance().getId();
		}
		catch (org.osid.repository.RepositoryException ignore) { }

 		return id;
	}

  public String getDisplayName()
                throws org.osid.repository.RepositoryException
  {
    return this.displayName;
  }

  public String getDescription()
                throws org.osid.repository.RepositoryException
  {
    return this.description;
  }

  public boolean isMandatory()
                 throws org.osid.repository.RepositoryException
  {
    return this.mandatory;
  }

  public boolean isPopulatedByRepository()
                 throws org.osid.repository.RepositoryException
  {
    return this.populatedByRepository;
  }

  public boolean isRepeatable() throws org.osid.repository.RepositoryException
  {
    return this.repeatable;
  }

  public void updateDisplayName(String displayName)
                      throws org.osid.repository.RepositoryException
  {
    throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
  }

  public org.osid.shared.Id getId()
                            throws org.osid.repository.RepositoryException
  {
    return this.PREFERREDURL_PART_STRUCTURE_ID;
  }

  public org.osid.shared.Type getType()
                              throws org.osid.repository.RepositoryException
  {
    return this.type;
  }

  public org.osid.repository.RecordStructure getRecordStructure()
                             throws org.osid.repository.RepositoryException
  {
    return RecordStructure.getInstance();
  }

  public boolean validatePart(org.osid.repository.Part part)
                 throws org.osid.repository.RepositoryException
  {
    return true;
  }

  public org.osid.repository.PartStructureIterator getPartStructures()
                             throws org.osid.repository.RepositoryException
  {
    return new PartStructureIterator(new java.util.Vector());
  }
}
