import { SITE_STATS_CHART_FALLBACK_THEME, siteStatsChartColor, siteStatsChartColors } from "./site-stats-chart-theme.js";

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
    const fallbacks = siteStatsChartColors(dataset.points.length, alpha, theme);
    const borderFallbacks = siteStatsChartColors(dataset.points.length, borderAlpha, theme);
    return {
      labels,
      datasets: [ {
        label: dataset.label,
        data: dataset.points.map(point => point.y),
        backgroundColor: pieColors(dataset, fallbacks, alpha, theme),
        borderColor: pieColors(dataset, borderFallbacks, borderAlpha, theme),
        borderWidth: useDepthEffect(chart) ? 2 : 1,
        hoverOffset: useDepthEffect(chart) ? 8 : 4,
      } ],
    };
  }

  const datasetCount = chart.datasets.length;
  const fillAlpha = chartType === "line" ? siteStatsChartAlpha(chart, 0.18) : siteStatsChartAlpha(chart, 0.65);
  const fillColors = siteStatsChartColors(datasetCount, fillAlpha, theme);
  const borderColors = siteStatsChartColors(datasetCount, borderAlpha, theme);
  const stackedSingle = stackedSingleCategory(chart);
  return {
    labels,
    datasets: chart.datasets.map((dataset, index) => {
      const backgroundColor = datasetPaint(dataset, fillColors[index], fillAlpha, theme);
      const borderColor = datasetPaint(dataset, borderColors[index], borderAlpha, theme);
      return {
        key: dataset.key,
        label: dataset.label,
        data: dataset.points.map(point => point.y),
        borderColor,
        backgroundColor,
        borderWidth: chart?.stacked === true && chart?.compact === true ? 2 : (useDepthEffect(chart) ? 3 : 2),
        borderAlign: chart?.stacked === true && chart?.compact === true ? "inner" : undefined,
        fill: chartType !== "line",
        pointRadius: chartType === "line" ? 3 : undefined,
        tension: 0.2,
        borderRadius: stackedBarBorderRadius(chart, index),
        categoryPercentage: stackedSingle ? 1 : undefined,
        barPercentage: stackedSingle ? 1 : undefined,
      };
    }),
  };
}

export function siteStatsChartOptions(chart, theme = SITE_STATS_CHART_FALLBACK_THEME, showItemLabels = true, compact = false) {

  const chartType = siteStatsChartType(chart);
  const compactChart = compact || chart?.compact === true;
  const stacked = chart?.stacked === true;
  const horizontal = chart?.horizontal === true;
  const stackedSingle = stackedSingleCategory(chart);
  const showLegend = !compactChart && (chart.datasets.length > 1 || chartType === "pie");
  const showCategoryTicks = !compactChart || (horizontal && !stackedSingle);
  const stackTotal = stacked ? stackedTotal(chart) : 0;
  const compactHorizontal = compactChart && horizontal;
  const funnelMax = compactHorizontal && !stacked ? valueMax(chart) : 0;
  const categoryScale = {
    stacked,
    border: {
      color: theme.borderColor,
    },
    grid: {
      display: !compactChart,
      color: theme.gridColor,
    },
    ticks: {
      display: showCategoryTicks,
      autoSkip: !showCategoryTicks,
      maxTicksLimit: compactChart ? 8 : undefined,
      color: theme.mutedTextColor,
      maxRotation: 0,
      font: compactChart ? { size: 10 } : undefined,
    },
  };
  const valueScale = {
    stacked,
    beginAtZero: true,
    bounds: compactHorizontal ? "data" : undefined,
    grace: (stacked && stackTotal > 0) || (compactHorizontal && funnelMax > 0) ? 0 : undefined,
    max: stacked && stackTotal > 0 ? stackTotal : (compactHorizontal && funnelMax > 0 ? funnelMax : undefined),
    min: compactHorizontal ? 0 : undefined,
    border: {
      color: theme.borderColor,
    },
    grid: {
      display: !compactChart,
      color: theme.gridColor,
    },
    ticks: {
      display: !compactChart,
      color: theme.mutedTextColor,
    },
  };

  return {
    indexAxis: horizontal ? "y" : "x",
    responsive: true,
    maintainAspectRatio: false,
    clip: compactChart ? false : undefined,
    layout: {
      autoPadding: !compactChart || showCategoryTicks,
      padding: {
        top: showItemLabels && !compactChart && chartType !== "pie" && !horizontal ? 18 : 0,
      },
    },
    plugins: {
      legend: {
        display: showLegend,
        labels: {
          color: theme.textColor,
        },
      },
      title: {
        display: false,
        color: theme.textColor,
      },
      tooltip: compactChart ? {
        enabled: false,
        filter: item => compactTooltipValue(item) !== 0,
        callbacks: {
          label: context => compactTooltipLabel(context),
        },
      } : undefined,
    },
    elements: {
      bar: {
        borderSkipped: false,
        borderRadius: compactChart ? (stackedSingle ? 0 : 1) : (useDepthEffect(chart) ? 2 : 0),
      },
      line: {
        borderColor: theme.borderColor,
      },
      point: {
        borderColor: theme.backgroundColor,
      },
    },
    scales: chartType === "pie" ? {} : {
      x: horizontal ? valueScale : categoryScale,
      y: horizontal ? categoryScale : valueScale,
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

function datasetColor(dataset, fallback, alpha, theme) {

  if (!dataset?.color) return fallback;
  return siteStatsChartColor(dataset.color, alpha, theme) || fallback;
}

function datasetPaint(dataset, fallback, alpha, theme) {

  if (Array.isArray(dataset?.points) && dataset.points.some(point => point?.color)) {
    return dataset.points.map(point => siteStatsChartColor(point.color, alpha, theme) || fallback);
  }
  return datasetColor(dataset, fallback, alpha, theme);
}

function pieColors(dataset, fallbacks, alpha, theme) {

  if (Array.isArray(dataset?.points) && dataset.points.some(point => point?.color)) {
    return dataset.points.map((point, index) => siteStatsChartColor(point.color, alpha, theme) || fallbacks[index]);
  }
  if (dataset?.color) {
    return siteStatsChartColor(dataset.color, alpha, theme) || fallbacks[0];
  }
  return fallbacks;
}

function stackedSingleCategory(chart) {

  return chart?.stacked === true
    && chart?.compact === true
    && (chart.datasets?.[0]?.points?.length || 0) <= 1;
}

function compactTooltipValue(item) {

  if (item?.parsed?.x != null && item.chart?.options?.indexAxis === "y") {
    return Number(item.parsed.x);
  }
  return Number(item?.parsed?.y ?? item?.parsed?.x ?? 0);
}

export function compactTooltipLabel(context) {

  const value = compactTooltipValue(context);
  if (!value) return null;
  if (context.dataset?.key === "partial") {
    return `${context.dataset.label}: ${value}`;
  }
  const datasets = context.chart?.data?.datasets || [];
  const split = datasets.some((dataset, index) =>
    index !== context.datasetIndex && Number(dataset.data?.[context.dataIndex]) > 0);
  if (split) {
    return `${context.dataset.label}: ${value}`;
  }
  return String(value);
}

function valueMax(chart) {

  let max = 0;
  (chart?.datasets || []).forEach(dataset => {
    (dataset?.points || []).forEach(point => {
      const value = Number(point?.y || 0);
      if (value > max) max = value;
    });
  });
  return max;
}

function stackedTotal(chart) {

  if (!Array.isArray(chart?.datasets) || !chart.datasets.length) return 0;
  const points = chart.datasets[0].points?.length || 0;
  let max = 0;
  for (let i = 0; i < points; i++) {
    let total = 0;
    chart.datasets.forEach(dataset => {
      total += Number(dataset.points?.[i]?.y || 0);
    });
    if (total > max) max = total;
  }
  return max;
}

function stackedBarBorderRadius(chart, datasetIndex) {

  if (!stackedSingleCategory(chart)) return undefined;
  const first = chart.datasets.findIndex(dataset => Number(dataset.points?.[0]?.y) > 0);
  let last = -1;
  for (let i = chart.datasets.length - 1; i >= 0; i--) {
    if (Number(chart.datasets[i].points?.[0]?.y) > 0) {
      last = i;
      break;
    }
  }
  const radius = 4;
  if (datasetIndex === first && datasetIndex === last) return radius;
  if (datasetIndex === first) {
    return { topLeft: radius, bottomLeft: radius, topRight: 0, bottomRight: 0 };
  }
  if (datasetIndex === last) {
    return { topLeft: 0, bottomLeft: 0, topRight: radius, bottomRight: radius };
  }
  return 0;
}
