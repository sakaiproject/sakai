// Shim for fetch-mock's ESM build in web-test-runner.
// Needed because the browser runner cannot resolve the CommonJS glob-to-regexp dependency.
// Remove this file and the related importMap/transform in web-test-runner.config.mjs once
// fetch-mock publishes an ESM-resolvable glob-to-regexp (or we can import it directly).
export default function globToRegExp(glob, opts) {
  if (typeof glob !== "string") {
    throw new TypeError("Expected a string");
  }

  const str = String(glob);
  let reStr = "";

  const extended = opts ? Boolean(opts.extended) : false;
  const globstar = opts ? Boolean(opts.globstar) : false;
  let inGroup = false;
  const flags = opts && typeof opts.flags === "string" ? opts.flags : "";

  let c;
  for (let i = 0, len = str.length; i < len; i++) {
    c = str[i];

    switch (c) {
      case "/":
      case "$":
      case "^":
      case "+":
      case ".":
      case "(":
      case ")":
      case "=":
      case "!":
      case "|":
        reStr += "\\" + c;
        break;

      case "?":
        if (extended) {
          reStr += ".";
          break;
        }

      case "[":
      case "]":
        if (extended) {
          reStr += c;
          break;
        }

      case "{":
        if (extended) {
          inGroup = true;
          reStr += "(";
          break;
        }

      case "}":
        if (extended) {
          inGroup = false;
          reStr += ")";
          break;
        }

      case ",":
        if (inGroup) {
          reStr += "|";
          break;
        }
        reStr += "\\" + c;
        break;

      case "*": {
        const prevChar = str[i - 1];
        let starCount = 1;
        while (str[i + 1] === "*") {
          starCount++;
          i++;
        }
        const nextChar = str[i + 1];

        if (!globstar) {
          reStr += ".*";
        } else {
          const isGlobstar = starCount > 1
            && (prevChar === "/" || prevChar === undefined)
            && (nextChar === "/" || nextChar === undefined);

          if (isGlobstar) {
            reStr += "((?:[^/]*(?:\\/|$))*)";
            i++;
          } else {
            reStr += "([^/]*)";
          }
        }
        break;
      }

      default:
        reStr += c;
    }
  }

  if (!flags || !~flags.indexOf("g")) {
    reStr = "^" + reStr + "$";
  }

  return new RegExp(reStr, flags);
}
