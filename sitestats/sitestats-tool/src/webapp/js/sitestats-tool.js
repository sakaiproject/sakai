document.querySelector("#print-report")?.addEventListener("click", () => window.print());

const reportEditor = document.querySelector("#report-editor");

if (reportEditor) {
  const control = id => reportEditor.querySelector(`#${id}`);
  const setVisible = (id, visible) => {
    const element = control(id);
    if (element) {
      element.hidden = !visible;
    }
  };
  const setEnabled = (element, enabled) => {
    if (element) {
      element.disabled = !enabled;
    }
  };
  const selectedValues = select => new Set(Array.from(select?.selectedOptions ?? [], option => option.value));
  const selectFirstEnabled = select => {
    if (!select || select.selectedOptions.length > 0 && !select.selectedOptions[0].disabled) {
      return;
    }
    const option = Array.from(select.options).find(candidate => !candidate.disabled);
    if (option) {
      select.value = option.value;
    }
  };

  const what = control("report-what");
  const eventSelection = control("event-selection");
  const totalsBy = control("totals-by");
  const presentation = control("presentation");
  const chartType = control("chart-type");
  const chartSource = control("chart-source");
  const chartCategory = control("chart-category");
  const chartSeries = control("chart-series");

  const updateTotals = () => {
    const invalidByReportType = {
      "what-resources": new Set(["tool", "event"]),
      "what-visits": new Set(["tool", "resource", "resource-action"]),
      "what-events": new Set(["resource", "resource-action"]),
      "what-presences": new Set(["tool", "event", "resource", "resource-action"]),
    };
    const invalid = invalidByReportType[what?.value] ?? new Set();
    Array.from(totalsBy?.options ?? []).forEach(option => {
      option.disabled = invalid.has(option.value);
      if (option.disabled) {
        option.selected = false;
      }
    });
  };

  const updateChartSources = () => {
    const totals = selectedValues(totalsBy);
    [chartSource, chartCategory, chartSeries].forEach(select => {
      Array.from(select?.options ?? []).forEach(option => {
        option.disabled = option.value !== "none" && option.value !== "total" && !totals.has(option.value);
        if (option.disabled) {
          option.selected = false;
        }
      });
      selectFirstEnabled(select);
    });
  };

  const updateWhat = () => {
    const reportType = what?.value;
    const events = reportType === "what-events";
    const resources = reportType === "what-resources";
    setVisible("event-options", events);
    setVisible("tool-selection", events && eventSelection?.value === "what-events-bytool");
    setVisible("event-selection-list", events && eventSelection?.value === "what-events-byevent");
    setVisible("resource-options", resources);
    setEnabled(control("resource-action"), resources && control("limit-resource-action")?.checked);
    setEnabled(control("resource-ids"), resources && control("limit-resources")?.checked);
    updateTotals();
    updateChartSources();
  };

  const updateWhen = () => setVisible("custom-dates", control("report-when")?.value === "when-custom");

  const updateWho = () => {
    const who = control("report-who")?.value;
    setVisible("who-role-options", who === "who-role");
    setVisible("who-group-options", who === "who-groups");
    setVisible("who-custom-options", who === "who-custom");
  };

  const updateLimits = () => {
    const sorting = control("sort-results")?.checked;
    setVisible("sorting-options", sorting);
    setEnabled(control("sort-by"), sorting);
    setEnabled(control("sort-ascending"), sorting);

    const limited = control("limit-results")?.checked;
    setVisible("max-results-options", limited);
    setEnabled(control("max-results"), limited);
    if (!limited && control("max-results")) {
      control("max-results").value = "0";
    }
  };

  const updateChart = () => {
    const chartVisible = presentation?.value !== "how-presentation-table";
    const timeSeries = chartType?.value === "timeseries" || chartType?.value === "timeseriesbar";
    setVisible("chart-options", chartVisible);
    setVisible("chart-source-options", chartVisible && !timeSeries);
    setVisible("chart-category-options", chartVisible && chartType?.value === "bar");
    setVisible("chart-series-options", chartVisible && timeSeries);
    if (chartVisible && timeSeries) {
      const dateOption = Array.from(totalsBy?.options ?? []).find(option => option.value === "date");
      if (dateOption) {
        dateOption.selected = true;
      }
      if (chartSource) {
        chartSource.value = "date";
      }
    }
    updateChartSources();
  };

  what?.addEventListener("change", updateWhat);
  eventSelection?.addEventListener("change", updateWhat);
  control("limit-resource-action")?.addEventListener("change", updateWhat);
  control("limit-resources")?.addEventListener("change", updateWhat);
  control("report-when")?.addEventListener("change", updateWhen);
  control("report-who")?.addEventListener("change", updateWho);
  control("sort-results")?.addEventListener("change", updateLimits);
  control("limit-results")?.addEventListener("change", updateLimits);
  totalsBy?.addEventListener("change", updateChartSources);
  presentation?.addEventListener("change", updateChart);
  chartType?.addEventListener("change", updateChart);

  updateWhat();
  updateWhen();
  updateWho();
  updateLimits();
  updateChart();
}
