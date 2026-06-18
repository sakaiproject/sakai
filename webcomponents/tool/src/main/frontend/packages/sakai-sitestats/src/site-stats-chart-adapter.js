import { SITE_STATS_CHART_FALLBACK_THEME, siteStatsChartColors } from "./site-stats-chart-theme.js";

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

export function siteStatsChartData(chart, theme = SITE_STATS_CHART_FALLBACK_THEME) {

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
        backgroundColor: siteStatsChartColors(dataset.points.length, alpha, theme),
        borderColor: siteStatsChartColors(dataset.points.length, borderAlpha, theme),
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
      borderColor: siteStatsChartColors(datasetCount, borderAlpha, theme)[index],
      backgroundColor: siteStatsChartColors(datasetCount, chartType === "line" ? siteStatsChartAlpha(chart, 0.18) : siteStatsChartAlpha(chart, 0.65), theme)[index],
      borderWidth: useDepthEffect(chart) ? 3 : 2,
      fill: chartType !== "line",
      pointRadius: chartType === "line" ? 3 : undefined,
      tension: 0.2,
    })),
  };
}

export function siteStatsChartOptions(chart, theme = SITE_STATS_CHART_FALLBACK_THEME, showItemLabels = true) {

  const chartType = siteStatsChartType(chart);

  return {
    responsive: true,
    maintainAspectRatio: false,
    layout: {
      padding: {
        top: showItemLabels && chartType !== "pie" ? 18 : 0,
      },
    },
    plugins: {
      legend: {
        display: chart.datasets.length > 1 || chartType === "pie",
        labels: {
          color: theme.textColor,
        },
      },
      title: {
        display: false,
        color: theme.textColor,
      },
    },
    elements: {
      bar: {
        borderSkipped: false,
        borderRadius: useDepthEffect(chart) ? 2 : 0,
      },
      line: {
        borderColor: theme.borderColor,
      },
      point: {
        borderColor: theme.backgroundColor,
      },
    },
    scales: chartType === "pie" ? {} : {
      x: {
        grid: {
          color: theme.gridColor,
        },
        ticks: {
          autoSkip: true,
          color: theme.mutedTextColor,
          maxRotation: 0,
        },
      },
      y: {
        beginAtZero: true,
        grid: {
          color: theme.gridColor,
        },
        ticks: {
          color: theme.mutedTextColor,
        },
      },
    },
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
