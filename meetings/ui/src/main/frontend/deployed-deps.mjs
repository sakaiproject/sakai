import fs from "node:fs/promises";
import path from "node:path";

const FRONTEND_ROOT = process.cwd();

const LOCK_PATH = path.join(FRONTEND_ROOT, "package-lock.json");
const OUT_PATH = path.join(FRONTEND_ROOT, "deployed-js-deps.json");
const OUT_DIR = path.join(FRONTEND_ROOT, "..", "..", "..", "target");
const STATS_PATH = path.join(OUT_DIR, "stats.json");

let lockRaw = null;
let lock = {};
let packagesSection = {};

try {
  lockRaw = await fs.readFile(LOCK_PATH, "utf8");
  lock = JSON.parse(lockRaw);
  packagesSection = lock.packages ?? {};
} catch (error) {
  const message = error instanceof Error ? error.message : String(error);
  console.warn(`Failed to read or parse lockfile at ${LOCK_PATH}: ${message}`);
  packagesSection = {};
  lock = {};
}

const NODE_MODULES_SEGMENT = "node_modules/";

function normalizeModulePath(value) {
  return value.replace(/^\.?\//, "").replace(/\\/g, "/");
}

function extractPackageName(modulePath) {
  const normalized = normalizeModulePath(modulePath);
  const idx = normalized.lastIndexOf(NODE_MODULES_SEGMENT);
  if (idx === -1) return null;
  const rest = normalized.slice(idx + NODE_MODULES_SEGMENT.length);
  const parts = rest.split("/").filter(Boolean);
  if (parts.length === 0) return null;
  if (parts[0].startsWith("@") && parts.length >= 2) {
    return `${parts[0]}/${parts[1]}`;
  }
  return parts[0];
}

function collectModuleStrings(value, out) {
  if (typeof value === "string") {
    if (value.includes(NODE_MODULES_SEGMENT)) {
      out.add(value);
    }
    return;
  }

  if (Array.isArray(value)) {
    for (const item of value) {
      collectModuleStrings(item, out);
    }
    return;
  }

  if (value && typeof value === "object") {
    for (const entry of Object.values(value)) {
      collectModuleStrings(entry, out);
    }
  }
}

let statsRaw = null;
try {
  statsRaw = await fs.readFile(STATS_PATH, "utf8");
} catch {
  console.warn(
    `No stats.json found at ${path.relative(FRONTEND_ROOT, STATS_PATH)}; report will be empty.`
  );
}

const dependencyNames = new Set();
if (statsRaw) {
  try {
    const stats = JSON.parse(statsRaw);
    const moduleStrings = new Set();
    collectModuleStrings(stats, moduleStrings);
    for (const value of moduleStrings) {
      const name = extractPackageName(value);
      if (name) dependencyNames.add(name);
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    console.warn(
      `Failed to parse stats.json at ${path.relative(FRONTEND_ROOT, STATS_PATH)}: ${message}`
    );
  }
}

function resolveVersion(name) {
  const entry = packagesSection[`node_modules/${name}`];
  if (entry?.version) return entry.version;
  const legacyEntry = lock.dependencies?.[name];
  if (legacyEntry?.version) return legacyEntry.version;
  return null;
}

const deployed = [...dependencyNames].sort().map((name) => ({
  name,
  version: resolveVersion(name),
}));

const out = {
  generatedAt: new Date().toISOString(),
  source: "rollup stats",
  statsFile: path.relative(FRONTEND_ROOT, STATS_PATH),
  lockFile: "package-lock.json",
  deployedPackages: deployed,
};

try {
  await fs.mkdir(path.dirname(OUT_PATH), { recursive: true });
  await fs.writeFile(OUT_PATH, JSON.stringify(out, null, 2) + "\n", "utf8");
  console.log(`Wrote ${path.relative(FRONTEND_ROOT, OUT_PATH)} (${deployed.length} packages)`);
} catch (error) {
  const message = error instanceof Error ? error.message : String(error);
  console.error(`Failed to write report to ${OUT_PATH}: ${message}`);
}
