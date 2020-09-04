package org.sakaiproject.elfinder;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
public class SakaiFsItem {
    @Getter @Setter private String id;
    @EqualsAndHashCode.Exclude @Getter private String title;
    @Getter private SakaiFsVolume volume;
    @Getter private FsType type;

    public SakaiFsItem(SakaiFsVolume volume, FsType type) {
        this(null, null, volume, type);
    }

    public SakaiFsItem(String id, SakaiFsVolume volume, FsType type) {
        this(id, null, volume, type);
    }

    public SakaiFsItem(String id, String title, SakaiFsVolume volume, FsType type) {
        this.id = id;
        this.title = title;
        this.volume = volume;
        this.type = type;
    }
}
