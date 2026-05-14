document.addEventListener("DOMContentLoaded", () => {
  const canvas = document.getElementById("poll-results-chart");
  const select = document.getElementById("chart-type-selection");
  const chartData = window.pollResultsChartData;

  if (!canvas || typeof Chart === "undefined" || !chartData) {
    return;
  }

  const ALLOWED_TYPES = new Set(["bar", "pie"]);
  const DEFAULT_TYPE = "bar";
  const PIE_COLORS = [
    "#4e79a7", "#f28e2b", "#e15759", "#76b7b2", "#59a14f",
    "#edc948", "#b07aa1", "#ff9da7", "#9c755f", "#bab0ac"
  ];

  function getChartType() {
    const stored = localStorage.getItem("polls-chart-type");
    return ALLOWED_TYPES.has(stored) ? stored : DEFAULT_TYPE;
  }

  function saveChartType(type) {
    localStorage.setItem("polls-chart-type", type);
  }

  const rootStyles = getComputedStyle(document.documentElement);
  const textColor = rootStyles.getPropertyValue("--sakai-text-color-1").trim() || "#1f2933";
  const borderColor = rootStyles.getPropertyValue("--sakai-border-color").trim() || "#d6dde6";
  const barColor = rootStyles.getPropertyValue("--sakai-primary-color-1").trim() || "#0d6efd";

  let chart = null;

  function buildConfig(type) {
    if (type === "pie") {
      return {
        type: "pie",
        data: {
          labels: chartData.labels,
          datasets: [{
            data: chartData.votes,
            backgroundColor: PIE_COLORS.slice(0, chartData.labels.length)
          }]
        },
        options: {
          animation: false,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              display: true,
              position: "right",
              labels: { color: textColor }
            },
            tooltip: {
              callbacks: {
                label(context) {
                  const percentage = chartData.percentages[context.dataIndex] || "0%";
                  return `${context.label}: ${context.raw} ${chartData.votesLabel} (${percentage})`;
                }
              }
            }
          }
        }
      };
    }

    return {
      type: "bar",
      data: {
        labels: chartData.labels,
        datasets: [{
          data: chartData.votes,
          backgroundColor: barColor,
          borderRadius: 6,
          maxBarThickness: 36
        }]
      },
      options: {
        animation: false,
        maintainAspectRatio: false,
        indexAxis: "y",
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              label(context) {
                const percentage = chartData.percentages[context.dataIndex] || "0%";
                return `${chartData.votesLabel}: ${context.raw} (${percentage})`;
              }
            }
          }
        },
        scales: {
          x: {
            beginAtZero: true,
            ticks: { precision: 0, color: textColor },
            title: { display: true, text: chartData.votesLabel, color: textColor },
            grid: { color: borderColor }
          },
          y: {
            ticks: { color: textColor },
            title: { display: true, text: chartData.optionLabel, color: textColor },
            grid: { display: false }
          }
        }
      }
    };
  }

  function renderChart(type) {
    if (chart) {
      chart.destroy();
    }
    chart = new Chart(canvas, buildConfig(type));
  }

  const savedType = getChartType();
  if (select) {
    select.value = savedType;
    select.addEventListener("change", () => {
      const type = ALLOWED_TYPES.has(select.value) ? select.value : DEFAULT_TYPE;
      saveChartType(type);
      renderChart(type);
    });
  }

  renderChart(savedType);
});