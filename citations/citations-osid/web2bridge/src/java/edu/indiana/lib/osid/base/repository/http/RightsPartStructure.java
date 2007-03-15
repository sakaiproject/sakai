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

public class RightsPartStructure implements org.osid.repository.PartStructure {

		private static org.apache.commons.logging.Log	_log = edu.indiana.lib.twinpeaks.util.LogUtils.getLog(RightsPartStructure.class);
		
  private org.osid.shared.Id RIGHTS_PART_STRUCTURE_ID = null;
  private org.osid.shared.Type type = new Type( "mit.edu", "partStructure",
      "rights", "Rights" );
  private String displayName = "Rights";
  private String description = "Typically, Rights will contain a rights " +
    "management statement for the resource, or reference a service " +
    "providing such information. Rights information often encompasses " +
    "Intellectual Property Rights (IPR), Copyright, and various " +
    "Property Rights. If the Rights element is absent, no assumptions " +
    "may be made about any rights held in or over the resource.";
  private boolean mandatory = false;
  private boolean populatedByRepository = false;
  private boolean repeatable = true;

  private static RightsPartStructure rightsPartStructure =
    new RightsPartStructure();

  protected static RightsPartStructure getInstance()
  {
    return rightsPartStructure;
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

  public boolean isRepeatable()
    throws org.osid.repository.RepositoryException
  {
    return this.repeatable;
  }

  protected RightsPartStructure()
  {
    try
    {
      this.RIGHTS_PART_STRUCTURE_ID = Managers.getIdManager().getId(
          "5492741f201080006d751920168000100");
    }
    catch (Throwable t)
    {
    }
  }

  public void updateDisplayName(String displayName)
    throws org.osid.repository.RepositoryException
  {
    throw new org.osid.repository.RepositoryException(
        org.osid.OsidException.UNIMPLEMENTED);
  }

  public org.osid.shared.Id getId()
    throws org.osid.repository.RepositoryException
  {
    return this.RIGHTS_PART_STRUCTURE_ID;
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
