export const SITE_STATS_CHART_FALLBACK_THEME = {
  textColor: "#1f2937",
  mutedTextColor: "#5f6773",
  borderColor: "#d8dde6",
  gridColor: "rgba(95, 103, 115, 0.24)",
  backgroundColor: "#ffffff",
  palette: [
    "rgb(25, 118, 210)",
    "rgb(46, 125, 50)",
    "rgb(239, 108, 0)",
    "rgb(123, 31, 162)",
    "rgb(0, 121, 107)",
    "rgb(198, 40, 40)",
    "rgb(69, 90, 100)",
    "rgb(245, 124, 0)",
  ],
};

export function siteStatsChartTheme(element) {

  if (!element || typeof getComputedStyle !== "function") return SITE_STATS_CHART_FALLBACK_THEME;

  const resolver = colorResolver(element);
  try {
    const textColor = cssColor(element, resolver, "--sakai-sitestats-chart-text-color", SITE_STATS_CHART_FALLBACK_THEME.textColor);
    const mutedTextColor = cssColor(element, resolver, "--sakai-sitestats-chart-muted-text-color", SITE_STATS_CHART_FALLBACK_THEME.mutedTextColor);
    const borderColor = cssColor(element, resolver, "--sakai-sitestats-chart-border-color", SITE_STATS_CHART_FALLBACK_THEME.borderColor);
    const backgroundColor = cssColor(element, resolver, "--sakai-sitestats-chart-background-color", SITE_STATS_CHART_FALLBACK_THEME.backgroundColor);

    return {
      textColor,
      mutedTextColor,
      borderColor,
      backgroundColor,
      gridColor: colorWithAlpha(borderColor, 0.55, resolver),
      palette: SITE_STATS_CHART_FALLBACK_THEME.palette.map((fallbackColor, index) =>
        cssColor(element, resolver, `--sakai-sitestats-chart-color-${index + 1}`, fallbackColor)),
    };
  } finally {
    resolver.destroy();
  }
}

export function siteStatsChartColors(count, alpha = 0.82, theme = SITE_STATS_CHART_FALLBACK_THEME) {

  const base = theme.palette?.length ? theme.palette : SITE_STATS_CHART_FALLBACK_THEME.palette;
  const colors = [];
  for (let i = 0; i < count; i++) {
    colors.push(colorWithAlpha(base[i % base.length], alpha));
  }
  return colors;
}

export function siteStatsChartThemeSignature(theme = SITE_STATS_CHART_FALLBACK_THEME) {

  return [
    theme.textColor,
    theme.mutedTextColor,
    theme.borderColor,
    theme.gridColor,
    theme.backgroundColor,
    ...(theme.palette || []),
  ].join("|");
}

function cssColor(element, resolver, name, fallback) {

  const value = getComputedStyle(element).getPropertyValue(name).trim() || fallback;
  return resolver.resolve(value, fallback);
}

function colorWithAlpha(color, alpha, resolver) {

  const number = Number(alpha);
  const safeAlpha = Number.isFinite(number) ? Math.min(1, Math.max(0, number)) : 1;
  const resolved = resolver ? resolver.resolve(color, color) : color;
  const rgb = rgbParts(resolved);
  if (!rgb) return resolved;
  return `rgba(${rgb[0]}, ${rgb[1]}, ${rgb[2]}, ${safeAlpha})`;
}

function colorResolver(element) {

  if (!element || typeof document === "undefined") {
    return {
      resolve: (color, fallback) => color || fallback,
      destroy: () => {},
    };
  }

  const probe = document.createElement("span");
  probe.style.position = "absolute";
  probe.style.visibility = "hidden";

  const root = element.renderRoot || element.shadowRoot || element;
  root.appendChild(probe);

  return {
    resolve: (color, fallback) => {
      probe.style.color = fallback;
      probe.style.color = color;
      return getComputedStyle(probe).color || fallback;
    },
    destroy: () => probe.remove(),
  };
}

function rgbParts(color) {

  const rgbMatch = color.match(/^rgba?\(\s*([0-9.]+)(?:,|\s)\s*([0-9.]+)(?:,|\s)\s*([0-9.]+)/i);
  if (!rgbMatch) return undefined;

  return [
    Math.round(Number(rgbMatch[1])),
    Math.round(Number(rgbMatch[2])),
    Math.round(Number(rgbMatch[3])),
  ];
}
