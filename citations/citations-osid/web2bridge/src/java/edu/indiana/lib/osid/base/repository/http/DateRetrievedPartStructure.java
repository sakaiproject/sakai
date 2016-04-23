package edu.indiana.lib.osid.base.repository.http;

public class DateRetrievedPartStructure
implements org.osid.repository.PartStructure
{
	private org.osid.shared.Id DATE_RETRIEVED_PART_STRUCTURE_ID = null;
	private org.osid.shared.Type type = new Type( "sakaibrary", "partStructure",
			"dateRetrieved", "Date Retrieved" );
	private String displayName = "Date Retrieved";
	private String description = "Date Retrieved indicates the date this " +
			"resource was retrieved.";
	private boolean mandatory = false;
	private boolean populatedByRepository = false;
	private boolean repeatable = false;

	private static DateRetrievedPartStructure dateRetrievedPartStructure =
		new DateRetrievedPartStructure();

	protected static DateRetrievedPartStructure getInstance()
	{
		return dateRetrievedPartStructure;
	}

	public String getDisplayName()
	throws org.osid.repository.RepositoryException
	{
		return this.displayName;
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

	protected DateRetrievedPartStructure()
	{
		try
		{
			this.DATE_RETRIEVED_PART_STRUCTURE_ID = Managers.getIdManager().getId(
					"b1asbd8f09s3450909751910168500100");
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
		return this.DATE_RETRIEVED_PART_STRUCTURE_ID;
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

