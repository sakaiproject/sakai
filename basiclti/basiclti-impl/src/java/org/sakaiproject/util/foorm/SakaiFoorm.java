package org.sakaiproject.util.foorm;

import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

import java.sql.ResultSet;
import org.sakaiproject.util.ResourceLoader;

// rm Foorm.class ; javac Foorm.java ; java Foorm

public class SakaiFoorm extends Foorm {

    // Abstract to be overridden
    @Override
    public String htmlSpecialChars(String str)
    {
	return str;
    }

    // Abstract this away for testing purposes - return null if non existant
    @Override
    public String loadI18N(String str, Object loader)
    {
	if ( loader == null ) return null;
        if ( loader instanceof ResourceLoader) { 
		return ((ResourceLoader) loader).getString(str,null);
	}
	return super.loadI18N(str, loader);
    }

}
