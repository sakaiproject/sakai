export async function getRubricElement(url) {

  const response = await fetch(url, {
    credentials: "include",
    headers: { "Content-Type": "application/json" },
  });
  if (response.ok) {
    return (response.status !== 204)?await response.json():null;
  } else {
    console.error("Server error while getting rubric element from url ", url, response.statusText);
    return null;
  }
}

export async function updateAdhocRubric(url, body) {

  const response = await fetch(url, {
    method: "POST",
    cache: "no-cache",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    body,
  });
  if (response.ok) {
    return await response.json();
  } else {
    console.error("Server error while saving dynamic rubric", response.statusText);
    return null;
  }
}

export function updateEvaluation(url, body, method) {

  const response = fetch(url, {
    body,
    credentials: "include",
    headers: {
      "Content-Type": "application/json"
    },
    method
  }).then((r) => {
      if (r.ok) {
        return r.json();
      }
      throw new Error("Server error while saving rubric evaluation");
    });
}

const DynamicRubricsApi = {
  getRubricElement,
  updateAdhocRubric,
  updateEvaluation
};

export default DynamicRubricsApi;
