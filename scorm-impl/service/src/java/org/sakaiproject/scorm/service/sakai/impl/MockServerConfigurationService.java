package org.sakaiproject.scorm.service.sakai.impl;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.sakaiproject.component.api.ServerConfigurationService;

/**
 *
 * @author bjones86
 */
public class MockServerConfigurationService implements ServerConfigurationService
{
    @Override
    public String getAccessPath()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getAccessUrl()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public boolean getBoolean( String name, boolean dflt )
    {
        return dflt;
    }

    @Override
    public List<String> getCategoryGroups( String category )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public <T> T getConfig( String name, T defaultValue )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ConfigData getConfigData()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ConfigItem getConfigItem( String name )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public List<String> getDefaultTools( String category )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getGatewaySiteId()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getHelpUrl( String helpContext )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public int getInt( String name, int dflt )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Locale getLocaleFromString( String localeString )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getLoggedOutUrl()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public List<Pattern> getPatternList( String name, List<String> dflt )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getPortalUrl()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getRawProperty( String name )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getSakaiHomePath()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Locale[] getSakaiLocales()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getServerId()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getServerIdInstance()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getServerInstance()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getServerName()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Collection<String> getServerNameAliases()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getServerUrl()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getString( String name )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getString( String name, String dflt )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public List<String> getStringList( String name, List<String> dflt )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String[] getStrings( String name )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public long getLong(String name, long dflt) {
        return 0;
    }

    @Override
    public List<String> getToolCategories( String category )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Map<String, List<String>> getToolCategoriesAsMap( String category )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public List<String> getToolGroup( String category )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public List<String> getToolOrder( String category )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Map<String, String> getToolToCategoryMap( String category )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getToolUrl()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public List<String> getToolsRequired( String category )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getUserHomeUrl()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ConfigItem registerConfigItem( ConfigItem configItem )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public void registerListener( ConfigurationListener configurationListener )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public boolean toolGroupIsRequired( String groupName, String toolId )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public boolean toolGroupIsSelected( String groupName, String toolId )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Set<String> getCommaSeparatedListAsSet(String key) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

}
