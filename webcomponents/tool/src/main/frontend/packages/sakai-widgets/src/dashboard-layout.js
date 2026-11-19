export const PICKER_TILE_ID = "picker";

const clampSpan = value => {
  const n = Number(value);
  if (Number.isNaN(n) || n <= 0) {
    return 1;
  }
  return Math.max(1, Math.min(2, Math.floor(n)));
};

export const defaultTileSize = id => (id === "courses" ? { w: 1, h: 2 } : { w: 1, h: 1 });

export const tileId = tile => {
  if (typeof tile === "string") {
    return tile;
  }
  return tile?.id ?? "";
};

export const tileKey = tile => tileId(tile);

export const layoutIds = layout => (layout ?? []).map(tileId).filter(id => id && id !== PICKER_TILE_ID);

/** Missing/invalid w or h become 1. Unrecognized payloads yield []. */
export const normalizeLayout = raw => {
  if (!Array.isArray(raw)) {
    return [];
  }

  const tiles = [];
  for (const item of raw) {
    if (typeof item === "string") {
      if (item && item !== PICKER_TILE_ID) {
        tiles.push({ id: item, ...defaultTileSize(item) });
      }
      continue;
    }
    if (item && typeof item === "object") {
      const id = item.id;
      if (!id || id === PICKER_TILE_ID) {
        continue;
      }
      tiles.push({ id, w: clampSpan(item.w), h: clampSpan(item.h) });
    }
  }
  return tiles;
};

export const isPickerTile = tile => tileId(tile) === PICKER_TILE_ID;
