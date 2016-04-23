package edu.indiana.lib.osid.base.repository.http;

public class SharedProperties
implements org.osid.shared.Properties
{
    private java.util.Map map = new java.util.HashMap();
    private org.osid.shared.Type type = new Type("edu.mit","shared","empty");

    public SharedProperties()
    throws org.osid.shared.SharedException
    {
    }

    public SharedProperties(java.util.Map map
                          , org.osid.shared.Type type)
    throws org.osid.shared.SharedException
    {
        this.map = map;
        this.type = type;
    }

    public org.osid.shared.ObjectIterator getKeys()
    throws org.osid.shared.SharedException
    {
        return new ObjectIterator(new java.util.Vector(this.map.keySet()));
    }

    public java.io.Serializable getProperty(java.io.Serializable key)
    throws org.osid.shared.SharedException
    {
        if (this.map.containsKey(key))
        {
            return (java.io.Serializable)this.map.get(key);
        }
        else
        {
            throw new org.osid.shared.SharedException(org.osid.shared.SharedException.UNKNOWN_KEY);
        }
    }

    public org.osid.shared.Type getType()
    throws org.osid.shared.SharedException
    {
        return this.type;
    }
}
