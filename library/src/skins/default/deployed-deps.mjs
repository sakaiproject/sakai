import fs from "node:fs/promises";
import path from "node:path";

const FRONTEND_ROOT = process.cwd();

const LOCK_PATH = path.join(FRONTEND_ROOT, "package-lock.json");
const OUT_PATH = path.join(FRONTEND_ROOT, "deployed-js-deps.json");
const TARGET_DIR = path.join(FRONTEND_ROOT, "target");

let lockRaw = null;
let lock = {};
let packagesSection = {};

try {
  lockRaw = await fs.readFile(LOCK_PATH, "utf8");
  lock = JSON.parse(lockRaw);
  packagesSection = lock.packages ?? {};
} catch (error) {
  const message = error instanceof Error ? error.message : String(error);
  console.error(`Missing or invalid lockfile at ${LOCK_PATH}: ${message}`);
  packagesSection = {};
  lock = {};
}

const NODE_MODULES_SEGMENT = "node_modules/";

function normalizePath(value) {
  return value.replace(/^webpack:\/\//, "").replace(/^\.?\//, "").replace(/\\/g, "/");
}

function extractPackageName(sourcePath) {
  const normalized = normalizePath(sourcePath);
  const firstIdx = normalized.indexOf(NODE_MODULES_SEGMENT);
  if (firstIdx === -1) return null;
  const lastIdx = normalized.lastIndexOf(NODE_MODULES_SEGMENT);
  const rest = normalized.slice(lastIdx + NODE_MODULES_SEGMENT.length);
  const parts = rest.split("/").filter(Boolean);
  if (parts.length === 0) return null;
  let name = parts[0];
  let nameSegments = 1;
  if (parts[0].startsWith("@") && parts.length >= 2) {
    name = `${parts[0]}/${parts[1]}`;
    nameSegments = 2;
  }
  const packagePath = parts.slice(0, nameSegments).join("/");
  const fullLockKey = normalized.slice(
    firstIdx,
    lastIdx + NODE_MODULES_SEGMENT.length + packagePath.length
  );
  return { name, fullLockKey };
}

async function listFilesRecursive(dir, suffix) {
  const results = [];
  try {
    const entries = await fs.readdir(dir, { withFileTypes: true });
    for (const entry of entries) {
      const fullPath = path.join(dir, entry.name);
      if (entry.isDirectory()) {
        results.push(...(await listFilesRecursive(fullPath, suffix)));
      } else if (entry.isFile() && entry.name.endsWith(suffix)) {
        results.push(fullPath);
      }
    }
  } catch {
    return [];
  }
  return results;
}

async function listSourceMaps(dir) {
  return listFilesRecursive(dir, ".css.map");
}

async function listCssFiles(dir) {
  return listFilesRecursive(dir, ".css");
}

const mapFiles = await listSourceMaps(TARGET_DIR);
const cssFiles = await listCssFiles(TARGET_DIR);
const mapTargets = new Set(
  mapFiles.map((mapFile) => path.relative(TARGET_DIR, mapFile).replace(/\.map$/, ""))
);
const missingMaps = cssFiles
  .map((cssFile) => path.relative(TARGET_DIR, cssFile))
  .filter((cssFile) => !mapTargets.has(cssFile));
const dependencyNames = new Map();

for (const mapFile of mapFiles) {
  try {
    const raw = await fs.readFile(mapFile, "utf8");
    const map = JSON.parse(raw);
    for (const source of map.sources ?? []) {
      const info = extractPackageName(source);
      if (info) {
        if (!dependencyNames.has(info.name)) {
          dependencyNames.set(info.name, new Set());
        }
        dependencyNames.get(info.name).add(info.fullLockKey);
      }
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    console.error(`Failed to read or parse sourcemap at ${mapFile}: ${message}`);
  }
}

if (dependencyNames.size === 0) {
  console.warn(
    `No node_modules entries found in ${path.relative(FRONTEND_ROOT, TARGET_DIR)} sourcemaps; report will be empty.`
  );
}

if (missingMaps.length > 0) {
  console.warn(
    `Missing sourcemaps for ${missingMaps.length} CSS files: ${missingMaps.join(", ")}`
  );
}

function resolveVersion(name, fullLockKeys) {
  if (fullLockKeys) {
    for (const fullLockKey of fullLockKeys) {
      const nestedEntry = packagesSection[fullLockKey];
      if (nestedEntry?.version) return nestedEntry.version;
    }
  }
  const entry = packagesSection[`node_modules/${name}`];
  if (entry?.version) return entry.version;
  const legacyEntry = lock.dependencies?.[name];
  if (legacyEntry?.version) return legacyEntry.version;
  return null;
}

const deployed = [...dependencyNames.keys()].sort().map((name) => ({
  name,
  version: resolveVersion(name, dependencyNames.get(name)),
}));

const out = {
  generatedAt: new Date().toISOString(),
  source: "css sourcemaps",
  lockFile: "package-lock.json",
  deployedPackages: deployed,
};

try {
  await fs.mkdir(path.dirname(OUT_PATH), { recursive: true });
  await fs.writeFile(OUT_PATH, JSON.stringify(out, null, 2) + "\n", "utf8");
  console.log(`Wrote ${path.relative(FRONTEND_ROOT, OUT_PATH)} (${deployed.length} packages)`);
} catch (error) {
  const message = error instanceof Error ? error.message : String(error);
  console.warn(`Failed to write report to ${OUT_PATH}: ${message}`);
}
