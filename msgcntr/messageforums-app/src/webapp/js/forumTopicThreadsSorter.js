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

	function cellText(row, columnIndex) {
		return (row.cells[columnIndex]?.textContent || "").trim().toLocaleLowerCase();
	}

	function cellNumber(row, columnIndex) {
		const text = cellText(row, columnIndex);
		const number = parseFloat(text.replace(/[^0-9.-]/g, ""));
		return Number.isNaN(number) ? 0 : number;
	}

	function rowKey(row, columnIndex) {
		const text = cellText(row, columnIndex);
		const number = cellNumber(row, columnIndex);
		return text && /^[-+]?[\d,.]+$/.test(text) ? number : text;
	}

	function compareRows(columnIndex, ascending) {
		return function(left, right) {
			const leftValue = rowKey(left, columnIndex);
			const rightValue = rowKey(right, columnIndex);
			let result = 0;

			if (typeof leftValue === "number" && typeof rightValue === "number") {
				result = leftValue - rightValue;
			} else {
				result = String(leftValue).localeCompare(String(rightValue), undefined, { numeric: true });
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

	function descendantsByThread(descendants, columnIndex, ascending) {
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

		childGroups.sort((left, right) => compareRows(columnIndex, ascending)(left.child, right.child));
		childGroups.forEach(group => result.push(group.child, ...group.children));

		return result;
	}

	function sortTable(table, columnIndex, ascending) {
		const tbody = table.tBodies[0];
		if (!tbody) return;

		const rows = Array.from(tbody.rows);
		const sortMode = table.tHead.rows[0].cells[columnIndex]?.querySelector("[data-sakai-forum-sort]")?.dataset.sakaiForumSort;
		const sortFlat = sortMode === "author";
		const sortByThread = sortMode === "thread";
		const sortedRows = [];

		if (sortFlat) {
			sortedRows.push(...rows.sort(compareRows(columnIndex, ascending)));
		} else {
			const groups = buildGroups(rows).sort((left, right) =>
				compareRows(columnIndex, ascending)(left.parent, right.parent));

			groups.forEach(group => {
				sortedRows.push(group.parent);
				if (sortByThread) {
					sortedRows.push(...descendantsByThread(group.descendants, columnIndex, ascending));
				} else {
					sortedRows.push(...group.descendants.sort(compareRows(columnIndex, ascending)));
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

	function init(table) {
		if (!table?.tHead?.rows.length) return;

		const headers = Array.from(table.tHead.rows[0].cells);
		let expanded = true;

		headers.forEach((header, columnIndex) => {
			header.style.cursor = "pointer";

			if (columnIndex === 0) {
				header.querySelector("a")?.remove();
				header.addEventListener("click", event => {
					expanded = !expanded;
					const imageObj = event.target instanceof HTMLImageElement
						? event.target
						: event.currentTarget.querySelector("img");
					toggleThreadVisibility(table, expanded, imageObj);

					if (parent?.document?.querySelector("iframe.portletMainIframe") && window.mySetMainFrameHeight) {
						mySetMainFrameHeight(parent.document.querySelector("iframe.portletMainIframe").id);
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
				sortTable(table, columnIndex, ascending);
			});
		});
	}

	window.sakaiForumThreadsSorter = { init };
}());
