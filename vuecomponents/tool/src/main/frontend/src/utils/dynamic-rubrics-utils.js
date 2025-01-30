export function replacer(key, value) {
  if (key === "selected") { return undefined; }
  else if (key === "description") { return undefined; }
  else { return value; }
}
