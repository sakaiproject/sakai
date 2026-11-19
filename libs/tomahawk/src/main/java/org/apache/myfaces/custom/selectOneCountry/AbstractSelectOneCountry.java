/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.custom.selectOneCountry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

import jakarta.faces.component.UISelectItems;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;

import org.apache.myfaces.component.html.ext.HtmlSelectOneMenu;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;

/**
 * A localized list of countries choose box. The value binds to the 
 * country ISO 3166 code. This is the same code as for 
 * java.util.Locale.getCountry(). The official codes list is 
 * available here : 
 * 
 * http://www.iso.ch/iso/en/prods-services/iso3166ma/02iso-3166-code-lists/list-en1.html 
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * @JSFComponent
 *   name = "t:selectOneCountry"
 *   class = "org.apache.myfaces.custom.selectOneCountry.SelectOneCountry"
 *   tagClass = "org.apache.myfaces.custom.selectOneCountry.SelectOneCountryTag"
 * @since 1.1.7
 * @author Sylvain Vieujot (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (Wed, 03 Sep 2008) $
 */
public abstract class AbstractSelectOneCountry extends HtmlSelectOneMenu {
    public static final String COMPONENT_TYPE = "org.apache.myfaces.SelectOneCountry";
    private static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.SelectOneCountryRenderer";

    public AbstractSelectOneCountry() {
        setRendererType(DEFAULT_RENDERER_TYPE);
    }

    /**
     * Integer equals to the maximum number of characters in the country name.
     * 
     * @JSFProperty
     */
    public abstract Integer getMaxLength();
    
    /**
     * Integer equals to the maximum number of characters in the country name.
     * 
     * @JSFProperty
     */
    public abstract String getEmptySelection();

    private Set getFilterSet(){
        List selectItems = RendererUtils.getSelectItemList( this );
        Set set = new HashSet( selectItems.size() );

        for (Iterator i = selectItems.iterator(); i.hasNext(); )
            set.add( ((SelectItem)i.next()).getValue().toString().toUpperCase() );

        return set;
    }

    protected List getCountriesChoicesAsSelectItemList(){
        //return RendererUtils.getSelectItemList(component);

        Set filterSet = getFilterSet();

        String[] availableCountries = Locale.getISOCountries();

        Locale currentLocale;

        FacesContext facesContext = FacesContext.getCurrentInstance();
        UIViewRoot viewRoot = facesContext.getViewRoot();
        if( viewRoot != null )
            currentLocale = viewRoot.getLocale();
        else
            currentLocale = facesContext.getApplication().getDefaultLocale();


        TreeMap map = new TreeMap();
        // TreeMap is sorted according to the keys' natural order

        for(int i=0; i<availableCountries.length; i++){
            String countryCode = availableCountries[i];
            if( ! filterSet.isEmpty() && ! filterSet.contains(countryCode))
                continue;
            Locale tmp = new Locale(countryCode, countryCode);
            map.put(tmp.getDisplayCountry(currentLocale), countryCode);
        }

        List countriesSelectItems = new ArrayList( map.size() );
        if(getEmptySelection() != null)
            countriesSelectItems.add(new SelectItem("", getEmptySelection()));

        Integer maxLength = getMaxLength();
        int maxDescriptionLength = maxLength==null ? Integer.MAX_VALUE : maxLength.intValue();
        if( maxDescriptionLength < 5 )
            maxDescriptionLength = 5;

        for(Iterator i = map.keySet().iterator(); i.hasNext(); ){
            String countryName = (String) i.next();
            String countryCode = (String) map.get( countryName );
            String label;
            if( countryName.length() <= maxDescriptionLength )
                label = countryName;
            else
                label = countryName.substring(0, maxDescriptionLength-3)+"...";

            countriesSelectItems.add( new SelectItem(countryCode, label) );
        }

        return countriesSelectItems;
    }

    protected void validateValue(FacesContext context, Object value) {
        UISelectItems selectItems = new UISelectItems();
        selectItems.setTransient(true);
        selectItems.setValue(getCountriesChoicesAsSelectItemList());
        getChildren().add(selectItems);
        
        super.validateValue(context,value);
    }
}
