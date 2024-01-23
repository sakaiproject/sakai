/**
 * Copyright (c) 2024 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.microsoft.mediagallery.auxiliar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.microsoft.api.data.MicrosoftDriveItem;

import lombok.Data;

@Data
public class MediaGallerySessionBean {
	public static enum Type { USER, SHARED }
	
	/**
	 * Contains all hierarchy indexed by Type: USER, SHARED or {TeamId}
	 * 
	 * User -> [ FolderA1 -> [ ItemA11, ItemA12..., ItemA1N ],
	 * 			  FolderA2 -> [ FolderA21 -> [ItemA211, ItemA212..., ItemA21N],
	 * 							ItemA22
	 * 						  ],
	 * 			  ItemA1,
	 * 			  ItemA2
	 * 			]
	 * Shared -> []
	 * TeamA_id -> []
	 */
	Map<Object, List<MicrosoftDriveItem>> itemsByType = new HashMap<>();
	
	/**
	 * Contains all items (not folders) from all Teams, indexed by Type: USER, SHARED or {TeamId}
	 */
	Map<Object, List<MicrosoftDriveItem>> allItemsByType = new HashMap<>();
	
	/**
	 * Contains all items (not folders) indexed by {ItemId}
	 */
	Map<String, MicrosoftDriveItem> itemsMap = new HashMap<>();
	
	/**
	 * Contains all type's name indexed by Type: USER, SHARED or {TeamId}
	 * 
	 * USER		-> User
	 * SHARED	-> Shared
	 * TeamA_id	-> TeamA_name
	 */
	Map<Object, String> typesMap = new HashMap<>();

	public void addItem(Object type, MicrosoftDriveItem item) {
		allItemsByType.computeIfAbsent(type, k -> new ArrayList<>()).add(item);
		itemsMap.put(item.getId(), item);
		item.setPath(getTypeName(type) + (StringUtils.isNotBlank(item.getPath()) ? item.getPath() : ""));
	}
	
	public MicrosoftDriveItem getItem(String itemId) {
		return itemsMap.get(itemId);
	}
	
	public void addType(Object type, String name) {
		typesMap.put(type, name);
	}
	
	public String getTypeName(Object type) {
		return typesMap.containsKey(type) ? typesMap.get(type) : "";
	}
	
	public List<Object> getSortedTypeKeys(){
		Comparator<Object> comparator = (a, b) -> {
			if(a.getClass().equals(b.getClass())) {
				if(a instanceof Type) {
					Type aux_a = (Type)a;
					Type aux_b = (Type)b;
					if(aux_a.ordinal() == aux_b.ordinal()) {
						return 0;
					}
					if(aux_a.ordinal() > aux_b.ordinal()) {
						return 1;
					}
					return -1;
				}
				if(a instanceof String) {
					String aux_a = getTypeName(a);
					String aux_b = getTypeName(b);
					return aux_a.compareToIgnoreCase(aux_b);
				}
			}
			return 0;
		};
		List<Object> ret = new ArrayList<>(typesMap.keySet());
		ret.sort(comparator);
		return ret;
	}
	
	public void resetItems(Object type) {
		allItemsByType.remove(type);
		itemsByType.remove(type);
	}
}
