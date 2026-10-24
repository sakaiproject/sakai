import { expect } from "@open-wc/testing";
import { layoutIds, normalizeLayout, tileId } from "../src/dashboard-layout.js";

describe("dashboard-layout", () => {

  it("defaults missing spans to 1x1 (courses to 1x2)", () => {

    expect(normalizeLayout([ { id: "announcements" }, "calendar", "courses", "" ])).to.deep.equal([
      { id: "announcements", w: 1, h: 1 },
      { id: "calendar", w: 1, h: 1 },
      { id: "courses", w: 1, h: 2 },
    ]);
  });

  it("clamps tile spans and skips picker", () => {

    const tiles = normalizeLayout([
      { id: "courses", w: 3, h: 0 },
      { id: "picker", w: 2, h: 2 },
      { id: "grades", w: 2, h: 2 },
    ]);
    expect(tiles).to.deep.equal([
      { id: "courses", w: 2, h: 1 },
      { id: "grades", w: 2, h: 2 },
    ]);
  });

  it("returns empty for unrecognized payloads", () => {
    expect(normalizeLayout(null)).to.deep.equal([]);
    expect(normalizeLayout({ id: "courses" })).to.deep.equal([]);
  });

  it("exposes layout ids without picker", () => {

    expect(layoutIds([
      { id: "courses", w: 1, h: 1 },
      { id: "picker", w: 1, h: 1 },
    ])).to.deep.equal([ "courses" ]);
    expect(tileId("calendar")).to.equal("calendar");
  });
});
