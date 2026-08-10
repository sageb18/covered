import { useState } from 'react'
import { loadDemoScenario, solveSchedule } from './lib/api'
import EmployeeCard from './components/EmployeeCard'
import { sameWindow } from './lib/windows'
import EmployeeForm from './components/EmployeeForm'
import ShiftCard from './components/ShiftCard'
import ShiftForm from './components/ShiftForm'
import ScheduleResult from './components/ScheduleResult'

function App() {
  // employees is already the exact shape /api/solve expects, unavailability nested inside
  const [employees, setEmployees] = useState([])
  const [shifts, setShifts] = useState([])
  const [result, setResult] = useState(null)
  const [isSolving, setIsSolving] = useState(false)
  const [error, setError] = useState(null)

  async function handleLoadExample() {
    setError(null)
    try {
      const scenario = await loadDemoScenario()
      setEmployees(scenario.employees)
      setShifts(scenario.shifts)
      setResult(null)
    } catch (e) {
      setError(e.message)
    }
  }

  async function handleSolve() {
    setError(null)
    setIsSolving(true)
    try {
      setResult(await solveSchedule({ employees, shifts }))
    } catch (e) {
      setError(e.message)
      setResult(null)
    } finally {
      // finally, so the button un-disables even if the request threw
      setIsSolving(false)
    }
  }

  // Every edit below clears the result: a schedule for a team that has since changed is
  // worse than no schedule, because it looks current.

  function addEmployee(employee) {
    setEmployees([...employees, employee])
    setResult(null)
  }

  function removeEmployee(id) {
    setEmployees(employees.filter((employee) => employee.id !== id))
    setResult(null)
  }

  // The nested shape's one awkward case: a new employees array, a new object for the one
  // employee, and a new unavailability array inside it. Everyone else is passed through
  // untouched, so React only re-renders the card that actually changed.
  function addUnavailability(employeeId, window) {
    setEmployees(employees.map((employee) =>
      employee.id === employeeId
        ? { ...employee, unavailability: [...employee.unavailability, window] }
        : employee))
    setResult(null)
  }

  function removeUnavailability(employeeId, window) {
    setEmployees(employees.map((employee) =>
      employee.id === employeeId
        ? {
            ...employee,
            unavailability: employee.unavailability.filter((w) => !sameWindow(w, window)),
          }
        : employee))
    setResult(null)
  }

  function addShift(shift) {
    setShifts([...shifts, shift])
    setResult(null)
  }

  function removeShift(id) {
    setShifts(shifts.filter((shift) => shift.id !== id))
    setResult(null)
  }

  const canSolve = employees.length > 0 && shifts.length > 0 && !isSolving

  return (
    <div className="mx-auto max-w-4xl p-6 font-sans">
      <h1 className="text-2xl font-bold">Covered</h1>
      <p className="text-gray-600">Schedule solver</p>

      <div className="my-4 flex gap-2">
        <button
          onClick={handleLoadExample}
          className="rounded border px-3 py-1 hover:bg-gray-100"
        >
          Load example
        </button>
        <button
          onClick={handleSolve}
          disabled={!canSolve}
          className="rounded border px-3 py-1 hover:bg-gray-100 disabled:opacity-40"
        >
          {isSolving ? 'Solving...' : 'Generate schedule'}
        </button>
      </div>

      {error && (
        <p className="my-2 rounded border border-red-300 bg-red-50 p-2 text-red-800">
          {error}
        </p>
      )}

      <div className="grid gap-6 md:grid-cols-2">
        <section>
          <h2 className="mb-2 text-lg font-semibold">
            Team <span className="text-sm font-normal text-gray-600">({employees.length})</span>
          </h2>
          <EmployeeForm onAdd={addEmployee} />
          {employees.length === 0 ? (
            <p className="text-sm text-gray-500">No employees yet.</p>
          ) : (
            <ul className="flex flex-col gap-2">
              {employees.map((employee) => (
                <EmployeeCard
                  key={employee.id}
                  employee={employee}
                  onRemove={removeEmployee}
                  onAddUnavailability={addUnavailability}
                  onRemoveUnavailability={removeUnavailability}
                />
              ))}
            </ul>
          )}
        </section>

        <section>
          <h2 className="mb-2 text-lg font-semibold">
            Shifts <span className="text-sm font-normal text-gray-600">({shifts.length})</span>
          </h2>
          <ShiftForm onAdd={addShift} />
          {shifts.length === 0 ? (
            <p className="text-sm text-gray-500">No shifts yet.</p>
          ) : (
            <ul className="flex flex-col gap-2">
              {shifts.map((shift) => (
                <ShiftCard key={shift.id} shift={shift} onRemove={removeShift} />
              ))}
            </ul>
          )}
        </section>
      </div>

      {result && (
        <ScheduleResult result={result} employees={employees} shifts={shifts} />
      )}
    </div>
  )
}

export default App
