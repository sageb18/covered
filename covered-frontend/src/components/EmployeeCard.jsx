import UnavailabilityForm from './UnavailabilityForm'
import { windowKey } from '../lib/windows'

function EmployeeCard({ employee, onRemove, onAddUnavailability, onRemoveUnavailability }) {
  return (
    <li className="rounded border p-3">
      <div className="flex items-start justify-between gap-2">
        <div>
          <span className="font-medium">{employee.name}</span>
          <span className="ml-2 text-sm text-gray-600">
            max {employee.maxHours}h/week
          </span>
        </div>
        <button
          onClick={() => onRemove(employee.id)}
          className="text-sm text-gray-500 hover:text-red-700"
        >
          Remove
        </button>
      </div>

      <div className="mt-1 flex flex-wrap gap-1">
        {employee.skills.length === 0 ? (
          <span className="text-sm text-gray-500">no skills</span>
        ) : (
          employee.skills.map((skill) => (
            <span key={skill} className="rounded bg-gray-200 px-2 py-0.5 text-xs">
              {skill}
            </span>
          ))
        )}
      </div>

      {employee.unavailability.length > 0 && (
        <ul className="mt-1 text-sm text-gray-600">
          {employee.unavailability.map((window) => (
            <li key={windowKey(window)} className="flex items-center gap-2">
              <span>
                unavailable {window.dayOfWeek} {window.start}-{window.end}
              </span>
              <button
                onClick={() => onRemoveUnavailability(employee.id, window)}
                className="text-xs text-gray-500 hover:text-red-700"
              >
                remove
              </button>
            </li>
          ))}
        </ul>
      )}

      <UnavailabilityForm
        onAdd={(window) => onAddUnavailability(employee.id, window)}
      />
    </li>
  )
}

export default EmployeeCard
