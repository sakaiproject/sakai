export function hasSiteStatsChartData(chart) {

  return chart?.supported !== false
    && Array.isArray(chart?.datasets)
    && chart.datasets.some(dataset => Array.isArray(dataset.points) && dataset.points.length > 0);
}

export function siteStatsChartType(chart) {

  switch (chart?.type) {
    case "line":
    case "timeseries":
      return "line";
    case "pie":
      return "pie";
    default:
      return "bar";
  }
}

export function siteStatsChartData(chart) {

  const labels = chart.datasets[0].points.map(point => point.label ?? point.x);
  const chartType = siteStatsChartType(chart);
  const alpha = siteStatsChartAlpha(chart);
  const borderAlpha = Math.min(1, alpha + 0.22);
  if (chartType === "pie") {
    const dataset = chart.datasets[0];
    return {
      labels,
      datasets: [ {
        label: dataset.label,
        data: dataset.points.map(point => point.y),
        backgroundColor: siteStatsChartColors(dataset.points.length, alpha),
        borderColor: siteStatsChartColors(dataset.points.length, borderAlpha),
        borderWidth: useDepthEffect(chart) ? 2 : 1,
        hoverOffset: useDepthEffect(chart) ? 8 : 4,
      } ],
    };
  }

  const datasetCount = chart.datasets.length;
  return {
    labels,
    datasets: chart.datasets.map((dataset, index) => ({
      label: dataset.label,
      data: dataset.points.map(point => point.y),
      borderColor: siteStatsChartColors(datasetCount, borderAlpha)[index],
      backgroundColor: siteStatsChartColors(datasetCount, chartType === "line" ? siteStatsChartAlpha(chart, 0.18) : siteStatsChartAlpha(chart, 0.65))[index],
      borderWidth: useDepthEffect(chart) ? 3 : 2,
      fill: chartType !== "line",
      pointRadius: chartType === "line" ? 3 : undefined,
      tension: 0.2,
    })),
  };
}

export function siteStatsFallbackTable(chart, i18n) {

  if (!hasSiteStatsChartData(chart)) return undefined;

  const columns = [
    { key: "label", label: chart.xKey || i18n?.label, type: "text" },
    ...chart.datasets.map(dataset => ({ key: dataset.key, label: dataset.label, type: "number", align: "end" })),
  ];

  const maxRows = Math.max(...chart.datasets.map(dataset => dataset.points.length));
  const rows = [];
  for (let i = 0; i < maxRows; i++) {
    const cells = {};
    const firstPoint = chart.datasets[0].points[i] || {};
    cells.label = { raw: firstPoint.x, display: firstPoint.label ?? firstPoint.x };
    chart.datasets.forEach(dataset => {
      const point = dataset.points[i] || {};
      cells[dataset.key] = { raw: point.y, display: point.y == null ? "" : String(point.y) };
    });
    rows.push({ cells });
  }

  return {
    caption: chart.title,
    columns,
    rows,
    page: 1,
    pageSize: rows.length,
    totalRows: rows.length,
  };
}

export function useDepthEffect(chart) {

  return chart?.threeDimensional === true;
}

export function siteStatsChartAlpha(chart, multiplier = 1) {

  const transparency = Number(chart?.transparency);
  const alpha = Number.isFinite(transparency) ? transparency : 1;
  return Math.min(1, Math.max(0, alpha * multiplier));
}

export function siteStatsChartColors(count, alpha = 0.82) {

  const base = [
    [ 25, 118, 210 ],
    [ 46, 125, 50 ],
    [ 239, 108, 0 ],
    [ 123, 31, 162 ],
    [ 0, 121, 107 ],
    [ 198, 40, 40 ],
    [ 69, 90, 100 ],
    [ 245, 124, 0 ],
  ];
  const colors = [];
  for (let i = 0; i < count; i++) {
    const c = base[i % base.length];
    colors.push(`rgba(${c[0]}, ${c[1]}, ${c[2]}, ${alpha})`);
  }
  return colors;
}
