import fs from "node:fs/promises";
import path from "node:path";

const FRONTEND_ROOT = process.cwd();

const LOCK_PATH = path.join(FRONTEND_ROOT, "package-lock.json");
const OUT_PATH = path.join(FRONTEND_ROOT, "deployed-js-deps.json");
const BUNDLE_DIR = path.join(FRONTEND_ROOT, "target", "js");

const lockRaw = await fs.readFile(LOCK_PATH, "utf8");

const lock = JSON.parse(lockRaw);
const packagesSection = lock.packages ?? {};

const NODE_MODULES_SEGMENT = "node_modules/";

function normalizeModulePath(value) {
  return value.replace(/^webpack:\/\//, "").replace(/^\.?\//, "").replace(/\\/g, "/");
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

async function findReportPath() {
  const candidates = [
    path.join(BUNDLE_DIR, "report.json"),
    path.join(FRONTEND_ROOT, "target", "report.json"),
  ];
  for (const candidate of candidates) {
    try {
      await fs.access(candidate);
      return candidate;
    } catch {
      // ignore missing
    }
  }

  try {
    const entries = await fs.readdir(path.join(FRONTEND_ROOT, "target"), {
      withFileTypes: true,
    });
    for (const entry of entries) {
      if (!entry.isDirectory()) continue;
      const candidate = path.join(FRONTEND_ROOT, "target", entry.name, "report.json");
      try {
        await fs.access(candidate);
        return candidate;
      } catch {
        // ignore missing
      }
    }
  } catch {
    // ignore
  }

  return null;
}

function collectModuleEntries(container, out) {
  if (!container) return;
  const modules = Array.isArray(container) ? container : container.modules;
  if (Array.isArray(modules)) {
    for (const moduleEntry of modules) {
      out.push(moduleEntry);
      if (moduleEntry?.modules) collectModuleEntries(moduleEntry, out);
    }
  }
}

function collectModulePaths(report) {
  const moduleEntries = [];
  collectModuleEntries(report, moduleEntries);
  if (Array.isArray(report?.children)) {
    for (const child of report.children) {
      collectModuleEntries(child, moduleEntries);
    }
  }

  const modulePaths = new Set();
  for (const entry of moduleEntries) {
    const candidates = [
      entry?.resource,
      entry?.name,
      entry?.identifier,
      entry?.moduleName,
    ];
    for (const candidate of candidates) {
      if (typeof candidate === "string" && candidate.includes(NODE_MODULES_SEGMENT)) {
        modulePaths.add(candidate);
      }
    }
  }

  return modulePaths;
}

const reportPath = await findReportPath();
const dependencyNames = new Set();
if (reportPath) {
  const reportRaw = await fs.readFile(reportPath, "utf8");
  const report = JSON.parse(reportRaw);
  const modulePaths = collectModulePaths(report);
  for (const modulePath of modulePaths) {
    const name = extractPackageName(modulePath);
    if (name) dependencyNames.add(name);
  }
}

if (!reportPath) {
  console.warn("No webpack report file found; report will be empty.");
} else if (dependencyNames.size === 0) {
  console.warn("No node_modules entries found in webpack report; report will be empty.");
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
  source: "webpack report",
  bundleDir: path.relative(FRONTEND_ROOT, BUNDLE_DIR),
  reportFile: reportPath ? path.relative(FRONTEND_ROOT, reportPath) : null,
  lockFile: "package-lock.json",
  deployedPackages: deployed,
};

await fs.writeFile(OUT_PATH, JSON.stringify(out, null, 2) + "\n", "utf8");

console.log(`Wrote ${path.relative(FRONTEND_ROOT, OUT_PATH)} (${deployed.length} packages)`);
