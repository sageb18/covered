import { useState } from 'react'
import { loadDemoScenario, solveSchedule } from './lib/api'
import EmployeeCard from './components/EmployeeCard'
import { sameWindow } from './lib/windows'
import EmployeeForm from './components/EmployeeForm'
import ShiftCard from './components/ShiftCard'
import ShiftForm from './components/ShiftForm'
import ScheduleResult from './components/ScheduleResult'

function App() {
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

  function addEmployee(employee) {
    setEmployees([...employees, employee])
    setResult(null)
  }

  function removeEmployee(id) {
    setEmployees(employees.filter((employee) => employee.id !== id))
    setResult(null)
  }

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
    <div className="mx-auto max-w-5xl px-6 py-10 font-sans">
      <header className="flex flex-wrap items-end justify-between gap-4 border-b border-line pb-6">
        <div>
          <h1 className="text-2xl font-medium text-fg">Covered</h1>
          <p className="mt-1 text-fg-muted">Schedule solver</p>
        </div>
        <div className="flex gap-2">
          <button onClick={handleLoadExample} className="btn-secondary">
            Load example
          </button>
          <button onClick={handleSolve} disabled={!canSolve} className="btn-primary">
            {isSolving ? 'Solving…' : 'Generate schedule'}
          </button>
        </div>
      </header>

      {error && (
        <p className="mt-6 rounded-xl border border-red/40 bg-red/10 p-4 text-sm text-fg">
          {error}
        </p>
      )}

      <div className="mt-8 grid gap-8 md:grid-cols-2">
        <section>
          <h2 className="mb-3 text-lg font-medium text-fg">
            Team <span className="text-sm text-fg-subtle">({employees.length})</span>
          </h2>
          <EmployeeForm onAdd={addEmployee} />
          {employees.length === 0 ? (
            <p className="rounded-xl border border-dashed border-line bg-raised p-6 text-center text-sm text-fg-subtle">
              No employees yet.
            </p>
          ) : (
            <ul className="flex flex-col gap-4">
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
          <h2 className="mb-3 text-lg font-medium text-fg">
            Shifts <span className="text-sm text-fg-subtle">({shifts.length})</span>
          </h2>
          <ShiftForm onAdd={addShift} />
          {shifts.length === 0 ? (
            <p className="rounded-xl border border-dashed border-line bg-raised p-6 text-center text-sm text-fg-subtle">
              No shifts yet.
            </p>
          ) : (
            <ul className="flex flex-col gap-4">
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
