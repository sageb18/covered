import { useState } from 'react'
import { DAYS, normaliseSkill } from '../lib/days'

function ShiftForm({ onAdd }) {
  const [dayOfWeek, setDayOfWeek] = useState('MONDAY')
  const [start, setStart] = useState('09:00')
  const [end, setEnd] = useState('17:00')
  const [requiredSkill, setRequiredSkill] = useState('')

  const skill = normaliseSkill(requiredSkill)
  const canSubmit = skill !== '' && start < end

  function handleSubmit(event) {
    event.preventDefault()
    if (!canSubmit) return

    onAdd({ id: crypto.randomUUID(), dayOfWeek, start, end, requiredSkill: skill })
    setRequiredSkill('')
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="mb-4 flex flex-col gap-3 rounded-xl border border-line bg-surface p-5"
    >
      <div className="flex gap-2">
        <select
          value={dayOfWeek}
          onChange={(event) => setDayOfWeek(event.target.value)}
          className="input min-w-0 flex-1"
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
          className="input"
          aria-label="Start time"
        />
        <input
          value={end}
          onChange={(event) => setEnd(event.target.value)}
          type="time"
          className="input"
          aria-label="End time"
        />
      </div>
      <input
        value={requiredSkill}
        onChange={(event) => setRequiredSkill(event.target.value)}
        placeholder="Required skill (e.g. barista)"
        className="input"
      />
      {start >= end && (
        <p className="text-sm text-amber">End time must be after start time.</p>
      )}
      <button type="submit" disabled={!canSubmit} className="btn-secondary self-start">
        Add shift
      </button>
    </form>
  )
}

export default ShiftForm
