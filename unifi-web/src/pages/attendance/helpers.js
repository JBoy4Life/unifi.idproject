import fp from 'lodash/fp'

export const getAttendanceRate = ({ presentCount, absentCount }) =>
  Math.round((presentCount / (presentCount + absentCount || 1)) * 100)

export const getAbsentCount = fp.compose(
  fp.size,
  fp.filter({ status: 'absent' })
)

export const getPresentCount = fp.compose(
  fp.size,
  fp.filter(item => ['present', 'auth-absent'].includes(item.status))
)

export const getProcessedCount = fp.compose(
  fp.size,
  fp.reject({ status: null })
)
