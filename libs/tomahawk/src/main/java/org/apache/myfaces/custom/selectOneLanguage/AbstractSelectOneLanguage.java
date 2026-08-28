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
package org.apache.myfaces.custom.selectOneLanguage;

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
 * A localized list of languages choose box. The value binds to the 
 * language ISO 639 code (lowercase). This is the same code as 
 * for java.util.Locale.getLanguage(). The official codes 
 * list is available here : 
 * 
 * http://www.loc.gov/standards/iso639-2/englangn.html 
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * @JSFComponent
 *   name = "t:selectOneLanguage"
 *   class = "org.apache.myfaces.custom.selectOneLanguage.SelectOneLanguage"
 *   tagClass = "org.apache.myfaces.custom.selectOneLanguage.SelectOneLanguageTag"
 * @since 1.1.7
 * @author Sylvain Vieujot (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (Wed, 03 Sep 2008) $
 */
public abstract class AbstractSelectOneLanguage extends HtmlSelectOneMenu {
    public static final String COMPONENT_TYPE = "org.apache.myfaces.SelectOneLanguage";
    private static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.SelectOneLanguageRenderer";

    private Integer _maxLength = null;
    
    private String _emptySelection = null;

    public AbstractSelectOneLanguage() {
        setRendererType(DEFAULT_RENDERER_TYPE);
    }

    /**
     * Integer equals to the maximum number of characters in the language name.
     * 
     * @JSFProperty
     */
    public abstract Integer getMaxLength();
    
    /**
     * Label and value to be used when displaying an empty selection
     * 
     * @JSFProperty
     */
    public abstract String getEmptySelection();

    private Set getFilterSet(){
        List selectItems = RendererUtils.getSelectItemList( this );
        Set set = new HashSet( selectItems.size() );

        for (Iterator i = selectItems.iterator(); i.hasNext(); )
            set.add( ((SelectItem)i.next()).getValue().toString().toLowerCase() );

        return set;
    }

    protected List getLanguagesChoicesAsSelectItemList(){
        //return RendererUtils.getSelectItemList(component);

        Set filterSet = getFilterSet();

        String[] availableLanguages = Locale.getISOLanguages();

        Locale currentLocale;

        FacesContext facesContext = FacesContext.getCurrentInstance();
        UIViewRoot viewRoot = facesContext.getViewRoot();
        if( viewRoot != null )
            currentLocale = viewRoot.getLocale();
        else
            currentLocale = facesContext.getApplication().getDefaultLocale();


        TreeMap map = new TreeMap();
        // TreeMap is sorted according to the keys' natural order

        for(int i=0; i<availableLanguages.length; i++){
            String languageCode = availableLanguages[i];
            if( ! filterSet.isEmpty() && ! filterSet.contains(languageCode))
                continue;
            Locale tmp = new Locale(languageCode);
            map.put(tmp.getDisplayLanguage(currentLocale), languageCode);
        }

        List languagesSelectItems = new ArrayList( map.size() );
        if(getEmptySelection() != null)
            languagesSelectItems.add(new SelectItem("", getEmptySelection()));

        Integer maxLength = getMaxLength();
        int maxDescriptionLength = maxLength==null ? Integer.MAX_VALUE : maxLength.intValue();
        if( maxDescriptionLength < 5 )
            maxDescriptionLength = 5;

        for(Iterator i = map.keySet().iterator(); i.hasNext(); ){
            String languageName = (String) i.next();
            String languageCode = (String) map.get( languageName );
            String label;
            if( languageName.length() <= maxDescriptionLength )
                label = languageName;
            else
                label = languageName.substring(0, maxDescriptionLength-3)+"...";

            languagesSelectItems.add( new SelectItem(languageCode, label) );
        }

        return languagesSelectItems;
    }

    protected void validateValue(FacesContext context, Object value) {
        UISelectItems selectItems = new UISelectItems();
        selectItems.setTransient(true);
        selectItems.setValue(getLanguagesChoicesAsSelectItemList());
        getChildren().add(selectItems);
        
        super.validateValue(context,value);
    }
}
