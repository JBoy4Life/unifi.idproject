import fp from 'lodash/fp'

export const getReducer = fp.get('attendance')

export const blocksSelector = fp.compose(
  fp.get('blocks'),
  getReducer
)

export const blockReportSelector = fp.compose(
  fp.get('blockReport'),
  getReducer
)

export const contactAttendanceSelector = fp.compose(
  fp.get('contactAttendance'),
  getReducer
)

export const schedulesSelector = fp.compose(
  fp.get('scheduleStats'),
  getReducer
)

export const overrideAttendanceResultSelector = fp.compose(
  fp.get('overrideAttendanceResult'),
  getReducer
)
