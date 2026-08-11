function ShiftCard({ shift, onRemove }) {
  return (
    <li className="flex items-start justify-between gap-3 rounded-xl border border-line bg-surface p-5">
      <div>
        <div className="font-medium text-fg">{shift.requiredSkill}</div>
        <div className="mt-0.5 text-sm text-fg-muted">
          <span className="text-fg-subtle">{shift.dayOfWeek}</span>{' '}
          {shift.start}–{shift.end}
        </div>
      </div>
      <button
        onClick={() => onRemove(shift.id)}
        className="text-sm text-fg-muted transition-colors hover:text-red"
      >
        Remove
      </button>
    </li>
  )
}

export default ShiftCard
