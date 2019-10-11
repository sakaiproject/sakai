package org.sakaiproject.elfinder.sakai.scorm;

import cn.bluejoe.elfinder.service.FsItem;
import cn.bluejoe.elfinder.service.FsVolume;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.sakaiproject.scorm.model.api.ContentPackage;

/**
 *
 * @author bjones86
 */
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode
public class ScormFsItem implements FsItem
{
    @NonNull @Getter
    private final String id;

    @NonNull @Getter
    private final FsVolume volume;

    @EqualsAndHashCode.Exclude @Getter
    private ContentPackage contentPackage;
}
