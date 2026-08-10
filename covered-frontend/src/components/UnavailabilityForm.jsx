import { useState } from 'react'
import { DAYS } from '../lib/days'

function UnavailabilityForm({ onAdd }) {
  const [dayOfWeek, setDayOfWeek] = useState('MONDAY')
  const [start, setStart] = useState('08:00')
  const [end, setEnd] = useState('12:00')

  const canSubmit = start < end

  function handleSubmit(event) {
    event.preventDefault()
    if (!canSubmit) return
    onAdd({ dayOfWeek, start, end })
  }

  return (
    <form onSubmit={handleSubmit} className="mt-2 flex flex-wrap items-center gap-1">
      <select
        value={dayOfWeek}
        onChange={(event) => setDayOfWeek(event.target.value)}
        className="rounded border px-1 py-0.5 text-xs"
        aria-label="Unavailable day"
      >
        {DAYS.map((day) => (
          <option key={day} value={day}>{day}</option>
        ))}
      </select>
      <input
        value={start}
        onChange={(event) => setStart(event.target.value)}
        type="time"
        className="rounded border px-1 py-0.5 text-xs"
        aria-label="Unavailable from"
      />
      <input
        value={end}
        onChange={(event) => setEnd(event.target.value)}
        type="time"
        className="rounded border px-1 py-0.5 text-xs"
        aria-label="Unavailable until"
      />
      <button
        type="submit"
        disabled={!canSubmit}
        className="rounded border px-2 py-0.5 text-xs hover:bg-gray-100 disabled:opacity-40"
      >
        Add unavailability
      </button>
    </form>
  )
}

export default UnavailabilityForm
