function ShiftCard({ shift, onRemove }) {
  return (
    <li className="flex items-center justify-between gap-2 rounded border p-3">
      <div>
        <span className="font-medium">{shift.requiredSkill}</span>
        <span className="ml-2 text-sm text-gray-600">
          {shift.dayOfWeek} {shift.start}-{shift.end}
        </span>
      </div>
      <button
        onClick={() => onRemove(shift.id)}
        className="text-sm text-gray-500 hover:text-red-700"
      >
        Remove
      </button>
    </li>
  )
}

export default ShiftCard
