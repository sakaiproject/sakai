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
  const csvValues = value => new Set((value ?? "").split(",").filter(Boolean));
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
  const sortBy = control("sort-by");
  const presentation = control("presentation");
  const chartType = control("chart-type");
  const chartSource = control("chart-source");
  const chartCategory = control("chart-category");
  const chartSeries = control("chart-series");
  const userSearch = control("who-user-search");
  const userSearchResults = control("who-user-search-results");
  const userSearchStatus = control("who-user-search-status");
  const selectedUsers = control("who-users");

  let userSearchTimer;
  let userSearchRequest;

  const messageWithName = (message, name) => message.replace("{0}", name);
  const searchUsers = async query => {
    userSearchRequest?.abort();
    userSearchRequest = new AbortController();
    userSearchStatus.textContent = userSearch.dataset.searching;
    const endpoint = new URL(userSearch.dataset.endpoint, window.location.href);
    endpoint.searchParams.set("q", query);
    try {
      const response = await fetch(endpoint, {
        headers: { Accept: "application/json" },
        signal: userSearchRequest.signal,
      });
      if (!response.ok) {
        throw new Error(`User search failed with status ${response.status}`);
      }
      const selectedIds = new Set(Array.from(selectedUsers.options, option => option.value));
      const users = (await response.json()).filter(user => !selectedIds.has(user.id));
      userSearchResults.replaceChildren(...users.map(user => {
        const item = document.createElement("li");
        item.className = "list-group-item p-1";
        const button = document.createElement("button");
        button.type = "button";
        button.className = "btn btn-link text-start w-100";
        button.textContent = messageWithName(userSearch.dataset.add, user.label);
        button.addEventListener("click", () => {
          selectedUsers.add(new Option(user.label, user.id, true, true));
          item.remove();
          userSearchStatus.textContent = messageWithName(userSearch.dataset.added, user.label);
          userSearch.focus();
        });
        item.append(button);
        return item;
      }));
      userSearchStatus.textContent = users.length > 0
        ? messageWithName(users.length === 1
          ? userSearch.dataset.resultOne : userSearch.dataset.resultMany, String(users.length))
        : userSearch.dataset.empty;
    } catch (error) {
      if (error.name !== "AbortError") {
        userSearchResults.replaceChildren();
        userSearchStatus.textContent = userSearch.dataset.error;
      }
    }
  };

  userSearch?.addEventListener("input", () => {
    window.clearTimeout(userSearchTimer);
    userSearchRequest?.abort();
    userSearchResults.replaceChildren();
    const query = userSearch.value.trim();
    if (query.length < 2) {
      userSearchStatus.textContent = "";
      return;
    }
    userSearchTimer = window.setTimeout(() => searchUsers(query), 300);
  });

  const updateTotals = () => {
    Array.from(totalsBy?.options ?? []).forEach(option => {
      const allowedReportTypes = csvValues(option.dataset.allowedFor);
      option.disabled = !allowedReportTypes.has(what?.value);
      if (option.disabled) {
        option.selected = false;
      }
    });
  };

  const updateChartSources = () => {
    const totals = selectedValues(totalsBy);
    Array.from(sortBy?.options ?? []).forEach(option => {
      option.disabled = option.dataset.requiresSelectedTotal === "true" && !totals.has(option.value);
      if (option.disabled) {
        option.selected = false;
      }
    });
    selectFirstEnabled(sortBy);
    [chartSource, chartCategory, chartSeries].forEach(select => {
      Array.from(select?.options ?? []).forEach(option => {
        const allowedChartTypes = csvValues(option.dataset.allowedChartTypes);
        const missingTotal = option.dataset.requiresSelectedTotal === "true" && !totals.has(option.value);
        option.disabled = !allowedChartTypes.has(chartType?.value) || missingTotal;
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
  };

  const updateChart = () => {
    const chartVisible = presentation?.value !== "how-presentation-table";
    const selectedChartType = chartType?.selectedOptions[0];
    const usesCategory = selectedChartType?.dataset.usesCategory === "true";
    const usesSeries = selectedChartType?.dataset.usesSeries === "true";
    setVisible("chart-options", chartVisible);
    setVisible("chart-source-options", chartVisible && !usesSeries);
    setVisible("chart-category-options", chartVisible && usesCategory);
    setVisible("chart-series-options", chartVisible && usesSeries);
    if (chartVisible) {
      const requiredTotal = selectedChartType?.dataset.requiredTotal;
      if (requiredTotal) {
        const requiredOption = Array.from(totalsBy?.options ?? [])
          .find(option => option.value === requiredTotal && !option.disabled);
        if (requiredOption) {
          requiredOption.selected = true;
        }
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
