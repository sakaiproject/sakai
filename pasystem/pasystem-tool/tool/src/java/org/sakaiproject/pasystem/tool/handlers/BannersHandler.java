/**********************************************************************************
 *
 * Copyright (c) 2015 The Sakai Foundation
 *
 * Original developers:
 *
 *   New York University
 *   Payten Giles
 *   Mark Triggs
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.pasystem.tool.handlers;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.cluster.api.ClusterService;
import org.sakaiproject.pasystem.api.Banner;
import org.sakaiproject.pasystem.api.PASystem;
import org.sakaiproject.pasystem.tool.forms.BannerForm;

/**
 * A handler for creating and updating banners in the PA System administration tool.
 */
@Slf4j
public class BannersHandler extends CrudHandler {

    private final PASystem paSystem;
    private  final ClusterService clusterService;

    public BannersHandler(PASystem pasystem, ClusterService clusterService) {
        this.paSystem = pasystem;
        this.clusterService = clusterService;
    }

    @Override
    protected void handleDelete(HttpServletRequest request) {
        String uuid = extractId(request);
        paSystem.getBanners().deleteBanner(uuid);

        flash("info", "banner_deleted");
        sendRedirect("");
    }

    @Override
    protected void handleEdit(HttpServletRequest request, Map<String, Object> context) {
        String uuid = extractId(request);
        context.put("subpage", "banner_form");
        context.put("hosts", clusterService.getServers().stream().sorted().collect(Collectors.toList()));

        Optional<Banner> banner = paSystem.getBanners().getForId(uuid);

        if (banner.isPresent()) {
            showEditForm(BannerForm.fromBanner(banner.get()), context, CrudMode.UPDATE);
        } else {
            log.warn("No banner found for UUID: " + uuid);
            sendRedirect("");
        }
    }

    @Override
    protected void showNewForm(Map<String, Object> context) {
        context.put("subpage", "banner_form");
        context.put("mode", "new");
        context.put("hosts", clusterService.getServers().stream().sorted().collect(Collectors.toList()));
    }

    @Override
    protected void handleCreateOrUpdate(HttpServletRequest request, Map<String, Object> context, CrudMode mode) {
        String uuid = extractId(request);
        BannerForm bannerForm = BannerForm.fromRequest(uuid, request);

        this.addErrors(bannerForm.validate());

        if (hasErrors()) {
            showEditForm(bannerForm, context, mode);
            return;
        }

        if (CrudMode.CREATE.equals(mode)) {
            paSystem.getBanners().createBanner(bannerForm.toBanner());
            flash("info", "banner_created");
        } else {
            paSystem.getBanners().updateBanner(bannerForm.toBanner());
            flash("info", "banner_updated");
        }

        sendRedirect("");
    }

    private void showEditForm(BannerForm bannerForm, Map<String, Object> context, CrudMode mode) {
        context.put("subpage", "banner_form");

        if (CrudMode.UPDATE.equals(mode)) {
            context.put("mode", "edit");
        } else {
            context.put("mode", "new");
        }

        context.put("banner", bannerForm);
    }
}
