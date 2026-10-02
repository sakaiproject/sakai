// Check if all parameters passed to methods are defined and not null
export function allParamsNonNull(...parameters) {
  return parameters.every(parameter => parameter !== null && parameter !== undefined);
}

// Check if all parameters passed to methods are defined and not null
export function queryParams(paramsObject) {
  if (!paramsObject || Object.keys(paramsObject).length === 0) { return ""; }

  return "?" + new URLSearchParams(paramsObject);
}

// Abstracted fetch logic
async function fetchData(responseHandler, ...params) {
  const response = await fetch(...params);

  if (response.ok) {
    return responseHandler(response);
  }
  console.error("Data could not be fetched:", {
    url: params[0],
    status: response.statusText
  });
  return null;

}

export async function fetchJson(...params) {
  return fetchData(response => response.json(), ...params);
}

export async function fetchText(...params) {
  return fetchData(async response => response.text(), ...params);
}
