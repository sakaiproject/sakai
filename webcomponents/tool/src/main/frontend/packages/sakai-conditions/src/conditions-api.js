import {
    allParamsNonNull,
    fetchJson,
    fetchText,
    queryParams,
} from "./condition-http.js";

import {
    ConditionType,
    lessonItemName,
    nonParentConditionFilter,
    nonRootConditionFilter,
} from "./condition-utils.js";

// Get conditions that are provided by the specified item
export async function getConditionsForItem(siteId, toolId, itemId) {
  if (!allParamsNonNull(siteId, toolId, itemId)) { return null; }

  return fetchJson(`/api/sites/${siteId}/conditions${queryParams({ toolId, itemId })}`);
}

export async function getRootCondition(siteId, toolId, itemId) {
  const conditions = await getConditionsForItem(siteId, toolId, itemId);

  if (conditions === null) { throw new Error("Unable to retrieve root condition"); }
  return conditions.find(c => c.type === ConditionType.ROOT);
}

// Get conditions that are available on the specified site
export async function getConditionsForSite(siteId) {
  if (!allParamsNonNull(siteId)) { return null; }

  const response = await fetch(`/api/sites/${siteId}/conditions`);

  if (response.ok) {
    return response.json();
  }
  console.error("Conditions could not be fetched:", response.statusText);
  return null;

}

export async function getToolItemsWithConditionsForLesson(siteId, lessonId) {
  if (!allParamsNonNull(siteId, lessonId)) { return null; }

  const lessonPromise = fetchJson(`/direct/lessons/lesson/${lessonId}.json`);
  const conditionsPromise = getConditionsForSite(siteId);

  const [ lesson, conditions ] = await Promise.all([ lessonPromise, conditionsPromise ]);

  if (!lesson?.contentsList || conditions === null) {
    console.error("Lesson or conditions not found");
    return null;
  }

  return lesson.contentsList.map(lessonItem => ({
    id: lessonItem.id,
    name: lessonItemName(lessonItem),
    conditions: conditions.filter(nonRootConditionFilter).filter(nonParentConditionFilter)
      .filter(condition => String(condition.itemId) === String(lessonItem.id)),
  })).filter(lessonItem => lessonItem.conditions.length > 0);
}

// Create condition
export async function createCondition(condition) {
  if (!condition || typeof condition !== "object") { return null; }

  return fetchJson(`/api/sites/${condition.siteId}/conditions`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(condition)
  });
}

// Update condition
export async function updateCondition(condition) {
  if (!condition || typeof condition !== "object") { return null; }

  return fetchJson(`/api/sites/${condition.siteId}/conditions/${condition.id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(condition)
  });
}

// Delete condition
export async function deleteCondition({ id: conditionId, siteId }) {
  if (!allParamsNonNull(siteId, conditionId)) { return null; }

  return fetchText(`/api/sites/${siteId}/conditions/${conditionId}`, {
    method: "DELETE",
  });
}
