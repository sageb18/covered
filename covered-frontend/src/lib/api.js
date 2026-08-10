// All paths are relative (/api/...), which works in dev through the Vite proxy and in
// production because Spring Boot serves this app from the same origin as the API.

async function request(path, options) {
  const response = await fetch(path, options)

  if (!response.ok) {
    throw new Error(await errorMessage(response))
  }
  return response.json()
}

// The backend sends { message, details: [...] } on a 400. Turn that into one readable line.
async function errorMessage(response) {
  try {
    const body = await response.json()
    if (body.message) {
      return body.details?.length
        ? `${body.message}: ${body.details.join(', ')}`
        : body.message
    }
  } catch {
    // not JSON - fall through to the status-code message
  }
  return `Request failed (${response.status} ${response.statusText})`
}

export function loadDemoScenario() {
  return request('/api/demo-scenario')
}

export function solveSchedule(scenario) {
  return request('/api/solve', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(scenario),
  })
}
