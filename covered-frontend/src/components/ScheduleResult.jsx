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
    <section className="mt-10">
      <div className="mb-4 flex items-center gap-3">
        <h2 className="text-lg font-medium text-fg">
          {result.feasible ? 'Schedule found' : 'No valid schedule'}
        </h2>
        <span
          className={`rounded-md px-2 py-1 text-xs ${
            result.feasible ? 'bg-teal/15 text-teal' : 'bg-red/15 text-red'
          }`}
        >
          {result.feasible ? 'Feasible' : 'Infeasible'}
        </span>
      </div>

      {/* Hard rules */}
      {!result.feasible && (
        <div className="mb-4 rounded-xl border border-red/40 bg-red/10 p-5">
          <p className="text-sm text-fg">Closest attempt shown below. Broken rules:</p>
          <ul className="mt-2 flex flex-col gap-1 text-sm text-fg-muted">
            {result.violations.map((violation) => (
              <li key={violation.constraint}>
                {violation.constraint}{' '}
                <span className="text-red">&times;{violation.count}</span>
              </li>
            ))}
          </ul>
        </div>
      )}

      {/* Soft rules */}
      {result.warnings?.length > 0 && (
        <div className="mb-4 rounded-xl border border-amber/40 bg-amber/10 p-5">
          <p className="text-sm text-fg">
            {result.feasible ? 'Schedule works, but' : 'Also worth noting'}:
          </p>
          <ul className="mt-2 flex flex-col gap-1 text-sm text-fg-muted">
            {result.warnings.map((warning) => (
              <li key={warning.constraint}>
                {warning.constraint}{' '}
                <span className="text-amber">&times;{warning.count}</span>
              </li>
            ))}
          </ul>
        </div>
      )}

      <div className="overflow-hidden rounded-xl border border-line bg-surface">
        <table className="w-full text-left text-sm">
          <thead>
            <tr className="border-b border-line text-xs text-fg-subtle">
              <th className="px-5 py-3 font-medium">Day</th>
              <th className="px-5 py-3 font-medium">Time</th>
              <th className="px-5 py-3 font-medium">Skill</th>
              <th className="px-5 py-3 font-medium">Assigned</th>
            </tr>
          </thead>
          <tbody>
            {rows.map(({ shift, employee }) => (
              <tr key={shift.id} className="border-b border-line last:border-b-0">
                <td className="px-5 py-3 text-fg-subtle">{shift.dayOfWeek}</td>
                <td className="px-5 py-3 text-fg-muted">{shift.start}–{shift.end}</td>
                <td className="px-5 py-3 text-fg">{shift.requiredSkill}</td>
                <td className="px-5 py-3">
                  {employee ? (
                    <span className="text-teal">{employee.name}</span>
                  ) : (
                    <span className="rounded-md border border-dashed border-line bg-raised px-2 py-1 text-xs text-red">
                      Unassigned
                    </span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  )
}

export default ScheduleResult
