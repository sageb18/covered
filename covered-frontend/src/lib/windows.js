// Unavailability windows have no id in the /solve contract, so they are identified by
// their three fields instead.

export function sameWindow(a, b) {
  return a.dayOfWeek === b.dayOfWeek && a.start === b.start && a.end === b.end
}

export function windowKey(window) {
  return `${window.dayOfWeek}-${window.start}-${window.end}`
}
