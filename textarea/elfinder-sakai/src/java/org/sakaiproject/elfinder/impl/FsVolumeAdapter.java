package org.sakaiproject.elfinder.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import org.sakaiproject.elfinder.SakaiFsItem;
import org.sakaiproject.elfinder.SakaiFsVolume;
import org.sakaiproject.exception.PermissionException;

import cn.bluejoe.elfinder.service.FsItem;
import cn.bluejoe.elfinder.service.FsVolume;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class FsVolumeAdapter implements FsVolume {

    @Getter private final SakaiFsVolume sakaiFsVolume;

    @Override
    public void createFile(FsItem fsi) throws IOException {
        SakaiFsItem sakaiFsItem = ((FsItemAdapter) fsi).getSakaiFsItem();
        sakaiFsVolume.createFile(sakaiFsItem);
    }

    @Override
    public void createFolder(FsItem fsi) throws IOException {
        SakaiFsItem sakaiFsItem = ((FsItemAdapter) fsi).getSakaiFsItem();
        sakaiFsVolume.createFolder(sakaiFsItem);
    }

    @Override
    public void deleteFile(FsItem fsi) throws IOException {
        SakaiFsItem sakaiFsItem = ((FsItemAdapter) fsi).getSakaiFsItem();
        sakaiFsVolume.deleteFile(sakaiFsItem);
    }

    @Override
    public void deleteFolder(FsItem fsi) throws IOException {
        SakaiFsItem sakaiFsItem = ((FsItemAdapter) fsi).getSakaiFsItem();
        sakaiFsVolume.deleteFolder(sakaiFsItem);
    }

    @Override
    public boolean exists(FsItem fsi) {
        SakaiFsItem sakaiFsItem = ((FsItemAdapter) fsi).getSakaiFsItem();
        return sakaiFsVolume.exists(sakaiFsItem);
    }

    @Override
    public FsItem fromPath(String relativePath) {
        return new FsItemAdapter(sakaiFsVolume.fromPath(relativePath));
    }

    @Override
    public String getDimensions(FsItem fsi) {
        SakaiFsItem sakaiFsItem = ((FsItemAdapter) fsi).getSakaiFsItem();
        return sakaiFsVolume.getDimensions(sakaiFsItem);
    }

    @Override
    public long getLastModified(FsItem fsi) {
        SakaiFsItem sakaiFsItem = ((FsItemAdapter) fsi).getSakaiFsItem();
        return sakaiFsVolume.getLastModified(sakaiFsItem);
    }

    @Override
    public String getMimeType(FsItem fsi) {
        SakaiFsItem sakaiFsItem = ((FsItemAdapter) fsi).getSakaiFsItem();
        return sakaiFsVolume.getMimeType(sakaiFsItem);
    }

    @Override
    public String getName() {
        return sakaiFsVolume.getName();
    }

    @Override
    public String getName(FsItem fsi) {
        SakaiFsItem sakaiFsItem = ((FsItemAdapter) fsi).getSakaiFsItem();
        return sakaiFsVolume.getName(sakaiFsItem);
    }

    @Override
    public FsItem getParent(FsItem fsi) {
        SakaiFsItem sakaiFsItem = ((FsItemAdapter) fsi).getSakaiFsItem();
        return new FsItemAdapter(sakaiFsVolume.getParent(sakaiFsItem));
    }

    @Override
    public String getPath(FsItem fsi) throws IOException {
        SakaiFsItem sakaiFsItem = ((FsItemAdapter) fsi).getSakaiFsItem();
        return sakaiFsVolume.getPath(sakaiFsItem);
    }

    @Override
    public FsItem getRoot() {
        return new FsItemAdapter(sakaiFsVolume.getRoot());
    }

    @Override
    public long getSize(FsItem fsi) throws IOException {
        SakaiFsItem sakaiFsItem = ((FsItemAdapter) fsi).getSakaiFsItem();
        return sakaiFsVolume.getSize(sakaiFsItem);
    }

    @Override
    public String getThumbnailFileName(FsItem fsi) {
        SakaiFsItem sakaiFsItem = ((FsItemAdapter) fsi).getSakaiFsItem();
        return sakaiFsVolume.getThumbnailFileName(sakaiFsItem);
    }

    @Override
    public boolean hasChildFolder(FsItem fsi) {
        SakaiFsItem sakaiFsItem = ((FsItemAdapter) fsi).getSakaiFsItem();
        return sakaiFsVolume.hasChildFolder(sakaiFsItem);
    }

    @Override
    public boolean isFolder(FsItem fsi) {
        SakaiFsItem sakaiFsItem = ((FsItemAdapter) fsi).getSakaiFsItem();
        return sakaiFsVolume.isFolder(sakaiFsItem);
    }

    @Override
    public boolean isRoot(FsItem fsi) {
        SakaiFsItem sakaiFsItem = ((FsItemAdapter) fsi).getSakaiFsItem();
        return sakaiFsVolume.isRoot(sakaiFsItem);
    }

    @Override
    public FsItem[] listChildren(FsItem fsi) {
        SakaiFsItem sakaiFsItem = ((FsItemAdapter) fsi).getSakaiFsItem();
        try {
            return Arrays.stream(sakaiFsVolume.listChildren(sakaiFsItem)).map(FsItemAdapter::new).toArray(FsItem[]::new);
        } catch (PermissionException e) {
            log.warn("Could not access children, {}", e.toString());
        }
        return new FsItem[0];
    }

    @Override
    public InputStream openInputStream(FsItem fsi) throws IOException {
        SakaiFsItem sakaiFsItem = ((FsItemAdapter) fsi).getSakaiFsItem();
        return sakaiFsVolume.openInputStream(sakaiFsItem);
    }

    @Override
    public void writeStream(FsItem fsi, InputStream is) throws IOException {
        SakaiFsItem sakaiFsItem = ((FsItemAdapter) fsi).getSakaiFsItem();
        sakaiFsVolume.writeStream(sakaiFsItem, is);
    }

    @Override
    public void rename(FsItem src, FsItem dst) throws IOException {
        sakaiFsVolume.rename(((FsItemAdapter) src).getSakaiFsItem(), ((FsItemAdapter) dst).getSakaiFsItem());
    }

    @Override
    public String getURL(FsItem fsi) {
        SakaiFsItem sakaiFsItem = ((FsItemAdapter) fsi).getSakaiFsItem();
        return sakaiFsVolume.getURL(sakaiFsItem);
    }

    @Override
    public void filterOptions(FsItem fsi, Map<String, Object> map) {
        SakaiFsItem sakaiFsItem = ((FsItemAdapter) fsi).getSakaiFsItem();
        sakaiFsVolume.filterOptions(sakaiFsItem, map);
    }
}
