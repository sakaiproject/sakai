document.addEventListener("DOMContentLoaded", () => {
  const canvas = document.getElementById("poll-results-chart");
  const chartData = window.pollResultsChartData;

  if (!canvas || typeof Chart === "undefined" || !chartData) {
    return;
  }

  const rootStyles = getComputedStyle(document.documentElement);
  const textColor = rootStyles.getPropertyValue("--sakai-text-color-1").trim() || "#1f2933";
  const borderColor = rootStyles.getPropertyValue("--sakai-border-color").trim() || "#d6dde6";
  const barColor = rootStyles.getPropertyValue("--sakai-primary-color-1").trim() || "#0d6efd";

  new Chart(canvas, {
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
        legend: {
          display: false
        },
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
          ticks: {
            precision: 0,
            color: textColor
          },
          title: {
            display: true,
            text: chartData.votesLabel,
            color: textColor
          },
          grid: {
            color: borderColor
          }
        },
        y: {
          ticks: {
            color: textColor
          },
          title: {
            display: true,
            text: chartData.optionLabel,
            color: textColor
          },
          grid: {
            display: false
          }
        }
      }
    }
  });
});
