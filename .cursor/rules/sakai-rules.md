# Cursor Rules for Sakai

Goals
- Keep Java code explicit and readable across the monorepo.
- Avoid modern shortcuts that reduce clarity in a large, long‑lived codebase.

Hard Rules
- Java: Never use local variable type inference (`var`). Always declare explicit types.
  - Yes: `Map<String, Integer> counts = new HashMap<>();`
  - No:  `var counts = new HashMap<String, Integer>();`
- Preserve existing code style and minimal diffs: do not reformat or rewrite unrelated code.

Assistant Behaviors
- When proposing Java code, spell out full types in local variable declarations, `for` loops, and try‑with‑resources.
- If input code contains `var`, suggest an explicit‑type version instead.
- When editing Java, prefer clarity over brevity; avoid introducing language features that aren’t widely used in the repo.

Review Gate
- Treat any PR or suggestion containing Java `var` as non‑compliant. Recommend replacing with explicit types before merge.

