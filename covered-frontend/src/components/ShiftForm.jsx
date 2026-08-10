import { useState } from 'react'
import { DAYS, normaliseSkill } from '../lib/days'

function ShiftForm({ onAdd }) {
  const [dayOfWeek, setDayOfWeek] = useState('MONDAY')
  const [start, setStart] = useState('09:00')
  const [end, setEnd] = useState('17:00')
  const [requiredSkill, setRequiredSkill] = useState('')

  const skill = normaliseSkill(requiredSkill)
  // string compare works because <input type="time"> always gives zero-padded HH:mm
  const canSubmit = skill !== '' && start < end

  function handleSubmit(event) {
    event.preventDefault()
    if (!canSubmit) return

    onAdd({ id: crypto.randomUUID(), dayOfWeek, start, end, requiredSkill: skill })
    setRequiredSkill('')
  }

  return (
    <form onSubmit={handleSubmit} className="mb-3 flex flex-col gap-2 rounded border p-3">
      <div className="flex gap-2">
        <select
          value={dayOfWeek}
          onChange={(event) => setDayOfWeek(event.target.value)}
          className="rounded border px-2 py-1"
          aria-label="Day of week"
        >
          {DAYS.map((day) => (
            <option key={day} value={day}>{day}</option>
          ))}
        </select>
        <input
          value={start}
          onChange={(event) => setStart(event.target.value)}
          type="time"
          className="rounded border px-2 py-1"
          aria-label="Start time"
        />
        <input
          value={end}
          onChange={(event) => setEnd(event.target.value)}
          type="time"
          className="rounded border px-2 py-1"
          aria-label="End time"
        />
      </div>
      <input
        value={requiredSkill}
        onChange={(event) => setRequiredSkill(event.target.value)}
        placeholder="Required skill (e.g. barista)"
        className="rounded border px-2 py-1"
      />
      {start >= end && (
        <p className="text-sm text-amber-700">End time must be after start time.</p>
      )}
      <button
        type="submit"
        disabled={!canSubmit}
        className="self-start rounded border px-3 py-1 hover:bg-gray-100 disabled:opacity-40"
      >
        Add shift
      </button>
    </form>
  )
}

export default ShiftForm
