/** HTTP boundary for Samigo's ad hoc rubrics and evaluations. */
async function request(url, options = {}) {
  const response = await fetch(url, {
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  if (response.status === 204 || (response.status === 404 && !options.method)) { return null; }
  if (!response.ok) { throw new Error(`Rubric request failed (${response.status}): ${url}`); }
  return response.json();
}

export function getRubricElement(url) {
  return request(url);
}

export function updateAdhocRubric(url, rubric) {
  return request(url, { method: "POST", cache: "no-cache", body: JSON.stringify(rubric) });
}

export function updateEvaluation(url, evaluation, method) {
  return request(url, { method, body: JSON.stringify(evaluation) });
}
