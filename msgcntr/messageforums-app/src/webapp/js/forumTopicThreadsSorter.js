/* Client side sorting for Sakai forums topic threads page. */
(function() {
	"use strict";

	function isParent(row) {
		return row.classList.contains("hierItemBlock");
	}

	function isDescendant(row) {
		return /_id_[0-9]+__hide_division_/.test(row.id);
	}

	function getDepth(row) {
		const cell = row.cells[1];
		if (!cell) return 0;

		const padding = getComputedStyle(cell).paddingLeft;
		if (padding.endsWith("px")) {
			return Math.round(parseFloat(padding) / parseFloat(getComputedStyle(document.body).fontSize || "16"));
		}

		return parseInt(padding, 10) || 0;
	}

	function cellText(row, columnIndex, locale) {
		return (row.cells[columnIndex]?.textContent || "").trim().toLocaleLowerCase(locale);
	}

	function cellNumber(row, columnIndex, locale) {
		const text = cellText(row, columnIndex, locale);
		const number = parseFloat(text.replace(/[^0-9.-]/g, ""));
		return Number.isNaN(number) ? 0 : number;
	}

	function rowKey(row, columnIndex, locale) {
		const text = cellText(row, columnIndex, locale);
		const number = cellNumber(row, columnIndex, locale);
		return text && /^[-+]?[\d,.]+$/.test(text) ? number : text;
	}

	function compareRows(columnIndex, ascending, locale) {
		return function(left, right) {
			const leftValue = rowKey(left, columnIndex, locale);
			const rightValue = rowKey(right, columnIndex, locale);
			let result = 0;

			if (typeof leftValue === "number" && typeof rightValue === "number") {
				result = leftValue - rightValue;
			} else {
				result = String(leftValue).localeCompare(String(rightValue), locale, { numeric: true });
			}

			return ascending ? result : -result;
		};
	}

	function buildGroups(rows) {
		const groups = [];
		let currentGroup = null;

		rows.forEach(row => {
			if (isParent(row) || !currentGroup) {
				currentGroup = { parent: row, descendants: [] };
				groups.push(currentGroup);
			} else if (isDescendant(row)) {
				currentGroup.descendants.push(row);
			} else {
				currentGroup = { parent: row, descendants: [] };
				groups.push(currentGroup);
			}
		});

		return groups;
	}

	function descendantsByThread(descendants, columnIndex, ascending, locale) {
		const result = [];
		const childGroups = [];
		let currentChildGroup = null;

		descendants.forEach(row => {
			if (getDepth(row) <= 1 || !currentChildGroup) {
				currentChildGroup = { child: row, children: [] };
				childGroups.push(currentChildGroup);
			} else {
				currentChildGroup.children.push(row);
			}
		});

		childGroups.sort((left, right) => compareRows(columnIndex, ascending, locale)(left.child, right.child));
		childGroups.forEach(group => result.push(group.child, ...group.children));

		return result;
	}

	function sortTable(table, columnIndex, ascending, locale) {
		const tbody = table.tBodies[0];
		if (!tbody) return;

		const rows = Array.from(tbody.rows);
		const sortMode = table.tHead.rows[0].cells[columnIndex]?.querySelector("[data-sakai-forum-sort]")?.dataset.sakaiForumSort;
		const sortFlat = sortMode === "author";
		const sortByThread = sortMode === "thread";
		const sortedRows = [];

		if (sortFlat) {
			sortedRows.push(...rows.sort(compareRows(columnIndex, ascending, locale)));
		} else {
			const groups = buildGroups(rows).sort((left, right) =>
				compareRows(columnIndex, ascending, locale)(left.parent, right.parent));

			groups.forEach(group => {
				sortedRows.push(group.parent);
				if (sortByThread) {
					sortedRows.push(...descendantsByThread(group.descendants, columnIndex, ascending, locale));
				} else {
					sortedRows.push(...group.descendants.sort(compareRows(columnIndex, ascending, locale)));
				}
			});
		}

		sortedRows.forEach(row => tbody.appendChild(row));
	}

	function setSortClasses(header, ascending) {
		header.parentElement.querySelectorAll("th").forEach(th => {
			th.classList.remove("headerSortDown", "headerSortUp");
			th.querySelector(".sakai-forum-sort-icon")?.remove();
		});
		header.classList.add(ascending ? "headerSortUp" : "headerSortDown");

		const icon = document.createElement("span");
		icon.className = `bi ${ascending ? "bi-caret-up-fill" : "bi-caret-down-fill"} sakai-forum-sort-icon`;
		icon.setAttribute("aria-hidden", "true");
		(header.querySelector("a") || header).appendChild(icon);
	}

	function toggleThreadVisibility(table, show, imageObj) {
		Array.from(table.tBodies[0].rows).forEach(row => {
			if (!isParent(row)) row.style.display = show ? "" : "none";
		});

		if (imageObj) {
			imageObj.src = show ? "../../images/expand-collapse.gif" : "../../images/collapse-expand.gif";
		}
		table.querySelectorAll("tr.hierItemBlock td:first-child img").forEach(img => {
			img.src = show ? "../../images/collapse.gif" : "../../images/expand.gif";
		});
	}

	function init(table, userLocale) {
		if (!table?.tHead?.rows.length) return;

		const headers = Array.from(table.tHead.rows[0].cells);
		const locale = userLocale.replaceAll("_", "-");
		let expanded = true;

		headers.forEach((header, columnIndex) => {
			header.style.cursor = "pointer";

			if (columnIndex === 0) {
				const toggleThreads = event => {
					expanded = !expanded;
					const imageObj = event.target instanceof HTMLImageElement
						? event.target
						: header.querySelector("img");
					toggleThreadVisibility(table, expanded, imageObj);
					header.setAttribute("aria-expanded", String(expanded));

					if (parent?.document?.querySelector("iframe.portletMainIframe") && window.mySetMainFrameHeight) {
						mySetMainFrameHeight(parent.document.querySelector("iframe.portletMainIframe").id);
					}
				};
				header.querySelector("a")?.remove();
				header.tabIndex = 0;
				header.setAttribute("role", "button");
				header.setAttribute("aria-expanded", "true");
				header.addEventListener("click", toggleThreads);
				header.addEventListener("keydown", event => {
					if (event.key === "Enter" || event.key === " " || event.keyCode === 13 || event.keyCode === 32) {
						event.preventDefault();
						toggleThreads(event);
					}
				});
				return;
			}

			if (!header.querySelector("[data-sakai-forum-sort]")) {
				header.style.cursor = "";
				return;
			}

			header.addEventListener("click", () => {
				const ascending = header.dataset.sortDirection !== "asc";
				header.dataset.sortDirection = ascending ? "asc" : "desc";
				setSortClasses(header, ascending);
				sortTable(table, columnIndex, ascending, locale);
			});
		});
	}

	window.sakaiForumThreadsSorter = { init };
}());
