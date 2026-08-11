import UnavailabilityForm from './UnavailabilityForm'
import { windowKey } from '../lib/windows'

function EmployeeCard({ employee, onRemove, onAddUnavailability, onRemoveUnavailability }) {
  return (
    <li className="rounded-xl border border-line bg-surface p-5">
      <div className="flex items-start justify-between gap-3">
        <div>
          <div className="font-medium text-fg">{employee.name}</div>
          <div className="mt-0.5 text-sm text-fg-muted">
            max {employee.maxHours}h/week
          </div>
        </div>
        <button
          onClick={() => onRemove(employee.id)}
          className="text-sm text-fg-muted transition-colors hover:text-red"
        >
          Remove
        </button>
      </div>

      <div className="mt-4 flex flex-wrap gap-1.5">
        {employee.skills.length === 0 ? (
          <span className="text-xs text-fg-subtle">No skills</span>
        ) : (
          employee.skills.map((skill) => (
            <span
              key={skill}
              className="rounded-md bg-raised px-2 py-1 text-xs text-fg-muted"
            >
              {skill}
            </span>
          ))
        )}
      </div>

      {employee.unavailability.length > 0 && (
        <div className="mt-4">
          <div className="mb-1.5 text-xs text-fg-subtle">Unavailable</div>
          <ul className="flex flex-col gap-1">
            {employee.unavailability.map((window) => (
              <li
                key={windowKey(window)}
                className="flex items-center justify-between gap-2 rounded-md bg-raised px-2 py-1 text-xs"
              >
                <span className="text-fg-muted">
                  <span className="text-fg-subtle">{window.dayOfWeek}</span>{' '}
                  {window.start}–{window.end}
                </span>
                <button
                  onClick={() => onRemoveUnavailability(employee.id, window)}
                  className="text-fg-subtle transition-colors hover:text-red"
                >
                  Remove
                </button>
              </li>
            ))}
          </ul>
        </div>
      )}

      <UnavailabilityForm
        onAdd={(window) => onAddUnavailability(employee.id, window)}
      />
    </li>
  )
}

export default EmployeeCard
