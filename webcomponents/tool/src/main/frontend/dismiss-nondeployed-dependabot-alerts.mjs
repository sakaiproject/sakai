import fs from "node:fs/promises";
import path from "node:path";
import process from "node:process";

// Dismiss Dependabot alerts for npm packages that are not deployed.
// Usage:
//   GITHUB_TOKEN=... node dismiss-nondeployed-dependabot-alerts.mjs --dry-run
//   GITHUB_TOKEN=... node dismiss-nondeployed-dependabot-alerts.mjs
// Options:
//   --owner <owner>         (default: sakaiproject)
//   --repo <repo>           (default: sakai)
//   --dry-run               (print dismiss actions without changes)
//   --frontend-dir <path>   (repeatable; default: cwd)
//   --verbose               (print skip details even without --dry-run)
//
// Example (all frontend dirs):
//   GITHUB_TOKEN=... node dismiss-nondeployed-dependabot-alerts.mjs --dry-run \
//     --frontend-dir ../../../webcomponents/tool/src/main/frontend \
//     --frontend-dir ../../../vuecomponents/tool/src/main/frontend \
//     --frontend-dir ../../../sakai/library/src/skins/default \
//     --frontend-dir ../../../sakai/meetings/ui/src/main/frontend
//
// The script compares Dependabot alerts against deployed-js-deps.json and
// dismisses alerts that reference packages not present in that report.

const DEFAULT_OWNER = "sakaiproject";
const DEFAULT_REPO = "sakai";
const DEFAULT_FRONTEND_DIR = process.cwd();
const API_BASE = "https://api.github.com";

function parseArgs(argv) {
  const args = {
    owner: DEFAULT_OWNER,
    repo: DEFAULT_REPO,
    frontendDirs: [],
    frontendDirArgs: 0,
    dryRun: false,
    verbose: false,
  };

  for (let i = 2; i < argv.length; i += 1) {
    const arg = argv[i];
    if (arg === "--owner") {
      const val = argv[++i];
      if (!val || val.startsWith("--")) {
        console.error("Missing value for --owner. Usage: --owner <owner>");
        process.exit(1);
      }
      args.owner = val;
    } else if (arg === "--repo") {
      const val = argv[++i];
      if (!val || val.startsWith("--")) {
        console.error("Missing value for --repo. Usage: --repo <repo>");
        process.exit(1);
      }
      args.repo = val;
    } else if (arg === "--frontend-dir") {
      const val = argv[++i];
      if (!val || val.startsWith("--")) {
        console.error("Missing value for --frontend-dir. Usage: --frontend-dir <path>");
        process.exit(1);
      }
      args.frontendDirs.push(val);
      args.frontendDirArgs += 1;
    } else if (arg === "--dry-run") args.dryRun = true;
    else if (arg === "--verbose") args.verbose = true;
  }

  if (args.frontendDirArgs === 0) {
    args.frontendDirs.push(DEFAULT_FRONTEND_DIR);
  }

  return args;
}

async function readDeployedSet(frontendDir) {
  const reportPath = path.join(frontendDir, "deployed-js-deps.json");
  try {
    const raw = await fs.readFile(reportPath, "utf8");
    const report = JSON.parse(raw);
    if (!Array.isArray(report.deployedPackages) || report.deployedPackages.length === 0) {
      console.error(
        `Missing or empty deployedPackages in deployed-js-deps.json at ${reportPath}.`
      );
      return null;
    }
    return new Set(report.deployedPackages.map((pkg) => pkg.name));
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    console.error(`Missing or invalid deployed-js-deps.json at ${reportPath}: ${message}`);
    return null;
  }
}

async function findRepoRoot(startDir) {
  let current = path.resolve(startDir);
  const root = path.parse(current).root;
  while (true) {
    try {
      await fs.access(path.join(current, ".git"));
      return current;
    } catch {
      if (current === root) return null;
      current = path.dirname(current);
    }
  }
}

function normalizePath(value) {
  return value.replace(/\\/g, "/");
}

function packageNameFromLockKey(key) {
  if (!key) return null;
  const marker = "node_modules/";
  const idx = key.lastIndexOf(marker);
  if (idx === -1) return null;
  return key.slice(idx + marker.length);
}

async function readLockfileIndex(frontendDir) {
  const lockPath = path.join(frontendDir, "package-lock.json");
  let lock = {};
  let packages = {};
  try {
    const raw = await fs.readFile(lockPath, "utf8");
    lock = JSON.parse(raw);
    packages = lock.packages ?? {};
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    console.error(`Missing or invalid package-lock.json at ${lockPath}: ${message}`);
    return null;
  }
  const byName = new Map();

  for (const [key, info] of Object.entries(packages)) {
    const name = packageNameFromLockKey(key);
    if (!name || !info?.version) continue;
    if (!byName.has(name)) byName.set(name, new Set());
    byName.get(name).add(info.version);
  }

  return { lockPath, byName };
}

async function githubRequest(token, url, options = {}) {
  const headers = {
    Accept: "application/vnd.github+json",
    Authorization: `Bearer ${token}`,
    "X-GitHub-Api-Version": "2022-11-28",
    "User-Agent": "sakai-dependabot-dismiss-script",
    ...(options.headers ?? {}),
  };
  const res = await fetch(url, {
    ...options,
    headers,
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`GitHub API error ${res.status}: ${text}`);
  }

  if (res.status === 204) return null;
  return res.json();
}

function getNextAfterCursor(linkHeader) {
  if (!linkHeader) return null;
  const parts = linkHeader.split(",");
  for (const part of parts) {
    const match = part.match(/<([^>]+)>;\s*rel="next"/);
    if (!match) continue;
    const url = new URL(match[1]);
    return url.searchParams.get("after");
  }
  return null;
}

async function listAlerts(token, owner, repo) {
  const alerts = [];
  let after = null;

  while (true) {
    const url = new URL(`${API_BASE}/repos/${owner}/${repo}/dependabot/alerts`);
    url.searchParams.set("state", "open");
    url.searchParams.set("per_page", "100");
    if (after) url.searchParams.set("after", after);

    const res = await fetch(url.toString(), {
      headers: {
        Accept: "application/vnd.github+json",
        Authorization: `Bearer ${token}`,
        "X-GitHub-Api-Version": "2022-11-28",
        "User-Agent": "sakai-dependabot-dismiss-script",
      },
    });

    if (!res.ok) {
      const text = await res.text();
      throw new Error(`GitHub API error ${res.status}: ${text}`);
    }

    const batch = await res.json();
    if (!Array.isArray(batch) || batch.length === 0) break;

    alerts.push(...batch);
    after = getNextAfterCursor(res.headers.get("link"));
    if (!after) break;
  }

  return alerts;
}

async function dismissAlert(token, owner, repo, alertNumber, comment) {
  const url = `${API_BASE}/repos/${owner}/${repo}/dependabot/alerts/${alertNumber}`;
  return githubRequest(token, url, {
    method: "PATCH",
    body: JSON.stringify({
      state: "dismissed",
      dismissal_reason: "not_used",
      dismissal_comment: comment,
    }),
    headers: {
      "Content-Type": "application/json",
    },
  });
}

const args = parseArgs(process.argv);
const token = process.env.GITHUB_TOKEN;
if (!token) {
  console.error("GITHUB_TOKEN is required.");
  process.exit(1);
}

const frontendDirs = [...new Set(args.frontendDirs.map((dir) => path.resolve(dir)))];
const repoRoot = await findRepoRoot(frontendDirs[0] ?? DEFAULT_FRONTEND_DIR);
if (repoRoot && args.frontendDirArgs === 0) {
  frontendDirs.splice(
    0,
    frontendDirs.length,
    path.join(repoRoot, "webcomponents/tool/src/main/frontend"),
    path.join(repoRoot, "vuecomponents/tool/src/main/frontend"),
    path.join(repoRoot, "library/src/skins/default"),
    path.join(repoRoot, "meetings/ui/src/main/frontend")
  );
  console.log(
    `No --frontend-dir provided; using default frontend dirs:\n  [${frontendDirs.join("]\n  [")}]`
  );
}
const contexts = await Promise.all(
  frontendDirs.map(async (frontendDir) => {
    const deployed = await readDeployedSet(frontendDir);
    if (!deployed) {
      process.exit(1);
    }
    const lockData = await readLockfileIndex(frontendDir);
    if (!lockData) {
      process.exit(1);
    }
    const { lockPath, byName: lockIndex } = lockData;
    return {
      frontendDir,
      deployed,
      lockIndex,
      manifestPath: normalizePath(lockPath),
      manifestPathRelative: repoRoot
        ? normalizePath(path.relative(repoRoot, lockPath))
        : null,
    };
  })
);
const alerts = await listAlerts(token, args.owner, args.repo);

const comment = "Not deployed. JS dependency appears only in build toolchain; bundled output excludes it.";

let dismissed = 0;
let simulatedDismissed = 0;
let failed = 0;
let skipped = 0;
const skipReasons = new Map();

function recordSkip(reason, details) {
  skipped += 1;
  const count = skipReasons.get(reason) ?? 0;
  skipReasons.set(reason, count + 1);
  if (args.verbose || args.dryRun) {
    console.log(`Skipping ${details} (${reason})`);
  }
}

function formatManifest(manifest) {
  if (!manifest) return "unknown-manifest";
  return normalizePath(manifest);
}

function formatDependencyLabel(name, reportedVersion, lockVersions) {
  if (!name) return "unknown";
  if (reportedVersion) return `${name}@${reportedVersion}`;
  if (lockVersions && lockVersions.size === 1) {
    const [onlyVersion] = lockVersions;
    return `${name}@${onlyVersion}`;
  }
  return name;
}

for (const alert of alerts) {
  const dep = alert?.dependency;
  const ecosystem = dep?.package?.ecosystem ?? dep?.ecosystem;
  const name = dep?.package?.name ?? dep?.name;
  const manifest = dep?.manifest_path ?? alert?.dependency_manifest_path;
  const reportedVersion = dep?.lockfile_version ?? dep?.manifest_version ?? dep?.version;
  let context = null;
  const baseLabel = formatDependencyLabel(name, reportedVersion, null);
  const manifestLabel = formatManifest(manifest);

  if (ecosystem !== "npm") {
    recordSkip(
      "non-npm ecosystem",
      `#${alert.number} ${baseLabel} (${ecosystem ?? "unknown"} ${manifestLabel})`
    );
    continue;
  }

  if (manifest) {
    const normalizedManifest = normalizePath(manifest);
    context = contexts.find(
      (entry) =>
        normalizedManifest === entry.manifestPath ||
        normalizedManifest === entry.manifestPathRelative
    );
    if (!context) {
      recordSkip(
        "manifest does not match any lockfile",
        `#${alert.number} ${baseLabel} (${ecosystem ?? "unknown"} ${manifestLabel})`
      );
      continue;
    }
  } else if (contexts.length === 1) {
    context = contexts[0];
  } else {
    recordSkip(
      "missing manifest path",
      `#${alert.number} ${baseLabel} (${ecosystem ?? "unknown"} ${manifestLabel})`
    );
    continue;
  }

  const lockVersions = name ? context.lockIndex.get(name) : null;
  const label = formatDependencyLabel(name, reportedVersion, lockVersions);

  if (!name) {
    recordSkip("missing dependency name", `#${alert.number} ${label}`);
    continue;
  }

  if (!lockVersions) {
    recordSkip(
      "dependency not in lockfile",
      `#${alert.number} ${label} (${ecosystem ?? "unknown"} ${manifestLabel})`
    );
    continue;
  }

  if (reportedVersion && !lockVersions.has(reportedVersion)) {
    recordSkip("lockfile version mismatch", `#${alert.number} ${label}`);
    continue;
  }

  if (context.deployed.has(name)) {
    recordSkip("dependency is deployed", `#${alert.number} ${label}`);
    continue;
  }

  if (args.dryRun) {
    console.log(`Would dismiss #${alert.number} (${label})`);
    simulatedDismissed += 1;
  } else {
    try {
      await dismissAlert(token, args.owner, args.repo, alert.number, comment);
      console.log(`Dismissed #${alert.number} (${label})`);
      dismissed += 1;
    } catch (error) {
      const message = error instanceof Error ? error.message : String(error);
      console.error(`Failed to dismiss #${alert.number} (${label}): ${message}`);
      failed += 1;
    }
  }
}

const dismissedLabel = args.dryRun ? simulatedDismissed : dismissed;
console.log(`Done. Dismissed ${dismissedLabel}, skipped ${skipped}, failed ${failed}.`);
if (skipReasons.size > 0) {
  console.log("Skip reasons:");
  for (const [reason, count] of skipReasons.entries()) {
    console.log(`- ${reason}: ${count}`);
  }
}
