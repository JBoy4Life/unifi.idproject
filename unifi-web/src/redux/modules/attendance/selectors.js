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

export const scheduleStatsSelector = fp.compose(
  fp.get('scheduleStats'),
  getReducer
)

export const schedulesSelector = fp.compose(
  fp.get('schedules'),
  getReducer
)

export const overrideAttendanceResultSelector = fp.compose(
  fp.get('overrideAttendanceResult'),
  getReducer
)

export const contactScheduleReportSelector = fp.compose(
  fp.get('contactScheduleReport'),
  getReducer
)

export const lowAttendanceReportSelector = fp.compose(
  fp.get('lowAttendanceReport'),
  getReducer
)

export const lowAttendanceReportStatusSelector = fp.compose(
  fp.get('lowAttendanceReportStatus'),
  getReducer
)
