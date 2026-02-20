import fs from "node:fs/promises";
import path from "node:path";

const FRONTEND_ROOT = process.cwd();

const META_PATH = path.join(FRONTEND_ROOT, "bundles", "meta.json");
const LOCK_PATH = path.join(FRONTEND_ROOT, "package-lock.json");
const OUT_PATH = path.join(FRONTEND_ROOT, "deployed-js-deps.json");

const NODE_MODULES_SEGMENT = "node_modules/";

function normalizePathSeparators(p) {
  return p.replace(/\\/g, "/");
}

function extractNodeModulesKey(p) {
  const normalized = normalizePathSeparators(p);
  const firstIdx = normalized.indexOf(NODE_MODULES_SEGMENT);
  if (firstIdx === -1) return null;
  const lastIdx = normalized.lastIndexOf(NODE_MODULES_SEGMENT);
  if (lastIdx === -1) return null;

  const rest = normalized.slice(lastIdx + NODE_MODULES_SEGMENT.length);
  if (!rest) return null;

  const parts = rest.split("/").filter(Boolean);
  if (parts.length === 0) return null;

  let packagePath = parts[0];
  // scoped package: @scope/name/...
  if (parts[0]?.startsWith("@") && parts.length >= 2) {
    packagePath = `${parts[0]}/${parts[1]}`;
  }

  // unscoped: name/...
  return normalized.slice(firstIdx, lastIdx + NODE_MODULES_SEGMENT.length + packagePath.length);
}

function packageNameFromNodeModulesKey(key) {
  if (!key?.includes(NODE_MODULES_SEGMENT)) return null;
  const idx = key.lastIndexOf(NODE_MODULES_SEGMENT);
  if (idx === -1) return null;
  const rest = key.slice(idx + NODE_MODULES_SEGMENT.length);
  if (!rest) return null;
  const parts = rest.split("/").filter(Boolean);
  if (parts.length === 0) return null;
  if (parts[0].startsWith("@") && parts.length >= 2) {
    return `${parts[0]}/${parts[1]}`;
  }
  return parts[0];
}

const [metaRaw, lockRaw] = await Promise.all([
  fs.readFile(META_PATH, "utf8"),
  fs.readFile(LOCK_PATH, "utf8"),
]);

const meta = JSON.parse(metaRaw);
const lock = JSON.parse(lockRaw);

const inputs = Object.keys(meta.inputs ?? {});
const pkgs = new Map();

for (const inputPath of inputs) {
  const nodeKey = extractNodeModulesKey(inputPath);
  if (!nodeKey) continue;
  const pkgName = packageNameFromNodeModulesKey(nodeKey);
  if (!pkgName) continue;
  if (pkgs.has(pkgName)) {
    const existing = pkgs.get(pkgName);
    const existingDepth = existing.nodeKey.split("/").filter(Boolean).length;
    const nextDepth = nodeKey.split("/").filter(Boolean).length;
    if (nextDepth < existingDepth) {
      pkgs.set(pkgName, { nodeKey });
    }
  } else {
    pkgs.set(pkgName, { nodeKey });
  }
}

const packagesSection = lock.packages ?? {};
const deployed = [];

async function readLocalPackageVersion(lockEntry) {
  const resolved = lockEntry?.resolved;
  if (!resolved || !lockEntry?.link) return null;
  const resolvedPath = resolved.startsWith("file:")
    ? resolved.replace(/^file:/, "")
    : resolved;
  const pkgPath = path.join(FRONTEND_ROOT, resolvedPath, "package.json");
  try {
    const raw = await fs.readFile(pkgPath, "utf8");
    const pkg = JSON.parse(raw);
    return pkg.version ?? null;
  } catch {
    return null;
  }
}

for (const name of [...pkgs.keys()].sort()) {
  const { nodeKey } = pkgs.get(name);
  const primaryEntry = packagesSection[nodeKey];
  const fallbackEntry = packagesSection[`${NODE_MODULES_SEGMENT}${name}`];

  let version = primaryEntry?.version ?? fallbackEntry?.version ?? null;
  if (!version) {
    version = await readLocalPackageVersion(primaryEntry)
      ?? await readLocalPackageVersion(fallbackEntry);
  }

  deployed.push({
    name,
    version,
  });
}

const out = {
  generatedAt: new Date().toISOString(),
  metaFile: "bundles/meta.json",
  lockFile: "package-lock.json",
  deployedPackages: deployed,
};

await fs.writeFile(OUT_PATH, JSON.stringify(out, null, 2) + "\n", "utf8");

console.log(`Wrote ${path.relative(FRONTEND_ROOT, OUT_PATH)} (${deployed.length} packages)`);
