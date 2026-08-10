// java.time.DayOfWeek enum names, in order. The backend parses these directly.
export const DAYS = [
  'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY',
]

// Skills are matched by exact string on the backend, so normalise both sides the same way
// or "barista" silently fails to match a shift needing "BARISTA".
export function normaliseSkill(text) {
  return text.trim().toUpperCase()
}
