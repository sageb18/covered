import { DAYS } from '../lib/days'

function indexById(items) {
  return Object.fromEntries(items.map((item) => [item.id, item]))
}

// /api/solve returns ids only, so the names and times come from the state we already hold.
function toRows(result, employees, shifts) {
  const employeesById = indexById(employees)
  const shiftsById = indexById(shifts)

  return result.assignments
    .map((assignment) => ({
      shift: shiftsById[assignment.shiftId],
      employee: employeesById[assignment.employeeId],
    }))
    .filter((row) => row.shift)
    .sort((a, b) =>
      DAYS.indexOf(a.shift.dayOfWeek) - DAYS.indexOf(b.shift.dayOfWeek) ||
      a.shift.start.localeCompare(b.shift.start))
}

function ScheduleResult({ result, employees, shifts }) {
  const rows = toRows(result, employees, shifts)

  return (
    <section className="mt-6">
      <h2 className="text-lg font-semibold">
        {result.feasible ? 'Schedule found' : 'No valid schedule'}
      </h2>

      {!result.feasible && (
        <div className="my-2 rounded border border-red-300 bg-red-50 p-3">
          <p className="text-sm text-red-900">
            Closest attempt shown below. Broken rules:
          </p>
          <ul className="mt-1 text-sm text-red-900">
            {result.violations.map((violation) => (
              <li key={violation.constraint}>
                {violation.constraint} &times;{violation.count}
              </li>
            ))}
          </ul>
        </div>
      )}

      {/* Soft rules, so this can sit on a schedule that is perfectly usable. */}
      {result.warnings?.length > 0 && (
        <div className="my-2 rounded border border-amber-300 bg-amber-50 p-3">
          <p className="text-sm text-amber-900">
            {result.feasible ? 'Schedule works, but' : 'Also worth noting'}:
          </p>
          <ul className="mt-1 text-sm text-amber-900">
            {result.warnings.map((warning) => (
              <li key={warning.constraint}>
                {warning.constraint} &times;{warning.count}
              </li>
            ))}
          </ul>
        </div>
      )}

      <table className="mt-2 w-full text-left text-sm">
        <thead>
          <tr className="border-b text-gray-600">
            <th className="py-1">Day</th>
            <th className="py-1">Time</th>
            <th className="py-1">Skill</th>
            <th className="py-1">Assigned</th>
          </tr>
        </thead>
        <tbody>
          {rows.map(({ shift, employee }) => (
            <tr key={shift.id} className="border-b">
              <td className="py-1">{shift.dayOfWeek}</td>
              <td className="py-1">{shift.start}-{shift.end}</td>
              <td className="py-1">{shift.requiredSkill}</td>
              <td className="py-1">
                {employee ? (
                  employee.name
                ) : (
                  <span className="text-red-700">unassigned</span>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  )
}

export default ScheduleResult
