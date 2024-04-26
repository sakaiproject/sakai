/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.portal.charon.handlers;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.tool.api.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

/**
 * Handler for manipulating the list of favorite sites for the current user.
 * Handles AJAX requests from the "More Sites" drawer.
 *
 */
@Slf4j
public class FavoritesHandler extends BasePortalHandler {
	private static final String URL_FRAGMENT = "favorites";

    @Autowired @Qualifier("org.sakaiproject.portal.api.PortalService")
    private PortalService portalService;

    public FavoritesHandler() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        setUrlFragment(URL_FRAGMENT);
    }


	@Override
	public int doGet(String[] parts, HttpServletRequest req,
			HttpServletResponse res, Session session)
			throws PortalHandlerException
	{
		if ((parts.length == 3) && (parts[1].equals(URL_FRAGMENT)))
		{
			try {
				res.setContentType(ContentType.APPLICATION_JSON.toString());
				res.getWriter().write(getUserFavorites(session.getUserId()).toJSON());
				return END;
			} catch (Exception e) {
				throw new PortalHandlerException(e);
			}
		}
		return NEXT;
	}

	@Override
	public int doPost(String[] parts, HttpServletRequest req,
			HttpServletResponse res, Session session)
			throws PortalHandlerException {

		if ((parts.length == 3) && (parts[1].equals(URL_FRAGMENT)))
		{
			try {
				FavoriteSites favorites = FavoriteSites.fromJSON(req.getParameter("userFavorites"));
				boolean reorder = StringUtils.equals("true", req.getParameter("reorder"));
				saveUserFavorites(session.getUserId(), favorites, reorder);
				res.setContentType(ContentType.APPLICATION_JSON.toString());
				return END;
			} catch (Exception e) {
				throw new PortalHandlerException(e);
			}
		}
		return NEXT;
	}

    public FavoriteSites getUserFavorites(String userId) {
        FavoriteSites favoriteSites = new FavoriteSites();
        favoriteSites.setFavoriteSiteIds(portalService.getPinnedSites(userId));
        return favoriteSites;
    }


    private void saveUserFavorites(String userId, FavoriteSites favorites, boolean reorder) {

        if (userId == null) return;

        if (reorder) {
            portalService.reorderPinnedSites(favorites.getFavoriteSiteIds());
        } else {
            portalService.savePinnedSites(favorites.getFavoriteSiteIds());
        }
    }

    public static class FavoriteSites {

        private Set<String> favoriteSiteIds;

        public FavoriteSites() {
            this.favoriteSiteIds = Collections.emptySet();
        }

        public List<String> getFavoriteSiteIds() {
            return new ArrayList<>(favoriteSiteIds);
        }

        public void setFavoriteSiteIds(List<String> favoriteSiteIds) {
            this.favoriteSiteIds = new LinkedHashSet<>(favoriteSiteIds);
        }

        public String toJSON() {
            StringWriter writer = new StringWriter();
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = mapper.createObjectNode();
            ArrayNode array = mapper.createArrayNode();
            favoriteSiteIds.forEach(array::add);
            root.set("favoriteSiteIds", array);

            try {
                mapper.writeValue(writer, root);
            } catch (IOException ioe) {
                log.warn("Could not serialize favorites, {}", ioe.toString());
            }
            return writer.toString();
        }

        public static FavoriteSites fromJSON(final String json) {
            FavoriteSites favoriteSites = new FavoriteSites();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode favoriteSiteIdsNode = objectMapper.readTree(json).get("favoriteSiteIds");
                if (favoriteSiteIdsNode.isArray()) {
                    List<String> siteIds = new ArrayList<>();
                    for (JsonNode node : favoriteSiteIdsNode) {
                        siteIds.add(node.asText());
                    }
                    favoriteSites.setFavoriteSiteIds(siteIds);
                } else {
                    log.warn("Unexpected element while parsing json: {}", json);
                }
            } catch (JsonProcessingException jpe) {
                log.warn("Could not parse json string [{}], {}", json, jpe.toString());
            }
            return favoriteSites;
        }
    }
}
