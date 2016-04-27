package edu.indiana.lib.osid.base.repository.http;

public class VolumePartStructure implements org.osid.repository.PartStructure
{
	private org.osid.shared.Id VOLUME_PART_STRUCTURE_ID = null;
    private org.osid.shared.Type type = new Type( "sakaibrary", "partStructure",
    		"volume", "Volume number of source" );
    private String displayName = "Volume";
    private String description = "Volume number of source (i.e. Journal title" +
    		", vol. 23)";
    private boolean mandatory = false;
    private boolean populatedByRepository = false;
    private boolean repeatable = false;

		private static VolumePartStructure volumePartStructure =
			new VolumePartStructure();

		public static VolumePartStructure getInstance()
		{
			return volumePartStructure;
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

    protected VolumePartStructure()
    {
        try
        {
            this.VOLUME_PART_STRUCTURE_ID =
            	Managers.getIdManager().getId("011293y7h98332do29873e1v000z02");
        }
        catch (Throwable t)
        {
        }
    }

    public void updateDisplayName(String displayName)
    throws org.osid.repository.RepositoryException
    {
        throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.shared.Id getId()
    throws org.osid.repository.RepositoryException
    {
        return this.VOLUME_PART_STRUCTURE_ID;
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
