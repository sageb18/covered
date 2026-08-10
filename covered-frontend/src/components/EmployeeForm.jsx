import { useState } from 'react'
import { normaliseSkill } from '../lib/days'

function parseSkills(text) {
  const skills = text.split(',').map(normaliseSkill).filter(Boolean)
  return [...new Set(skills)]
}

function EmployeeForm({ onAdd }) {
  const [name, setName] = useState('')
  const [maxHours, setMaxHours] = useState('40')
  const [skills, setSkills] = useState('')

  const trimmedName = name.trim()
  const hours = Number(maxHours)
  const canSubmit = trimmedName !== '' && hours > 0

  function handleSubmit(event) {
    // without this the browser reloads the page on submit and the state is wiped
    event.preventDefault()
    if (!canSubmit) return

    onAdd({
      id: crypto.randomUUID(),
      name: trimmedName,
      maxHours: hours,
      skills: parseSkills(skills),
      unavailability: [],
    })

    setName('')
    setMaxHours('40')
    setSkills('')
  }

  return (
    <form onSubmit={handleSubmit} className="mb-3 flex flex-col gap-2 rounded border p-3">
      <div className="flex gap-2">
        <input
          value={name}
          onChange={(event) => setName(event.target.value)}
          placeholder="Name"
          className="min-w-0 flex-1 rounded border px-2 py-1"
        />
        <input
          value={maxHours}
          onChange={(event) => setMaxHours(event.target.value)}
          type="number"
          min="1"
          className="w-24 rounded border px-2 py-1"
          aria-label="Max hours per week"
        />
      </div>
      <input
        value={skills}
        onChange={(event) => setSkills(event.target.value)}
        placeholder="Skills, comma separated (e.g. barista, closer)"
        className="rounded border px-2 py-1"
      />
      <button
        type="submit"
        disabled={!canSubmit}
        className="self-start rounded border px-3 py-1 hover:bg-gray-100 disabled:opacity-40"
      >
        Add employee
      </button>
    </form>
  )
}

export default EmployeeForm
