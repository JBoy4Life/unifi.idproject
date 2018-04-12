import createWsAction from 'utils/create-ws-action'

// ------------------------------------
// Action Types
// ------------------------------------
export const LIST_SCHEDULE_STATS = 'unifi.LIST_SCHEDULE_STATS'
export const LIST_SCHEDULES = 'unifi.LIST_SCHEDULES'
export const LIST_BLOCKS = 'unifi.LIST_BLOCKS'
export const GET_CONTACT_ATTENDANCE_FOR_SCHEDULE = 'unifi.GET_CONTACT_ATTENDANCE_FOR_SCHEDULE'
export const REPORT_BLOCK_ATTENDANCE = 'unifi.REPORT_BLOCK_ATTENDANCE'
export const REPORT_CONTACT_SCHEDULE_ATTENDANCE = 'unifi.REPORT_CONTACT_SCHEDULE_ATTENDANCE'
export const OVERRIDE_ATTENDANCE = 'unifi.OVERRIDE_ATTENDANCE'
export const REPORT_LOW_ATTENDANCE_BY_METADATA = 'unifi.REPORT_LOW_ATTENDANCE_BY_METADATA'

// ------------------------------------
// Action Creators
// ------------------------------------
export const listScheduleStats = createWsAction({
  type: LIST_SCHEDULE_STATS,
  messageType: 'attendance.schedule.list-schedule-stats',
  selectorKey: 'scheduleStats',
  fields: ['clientId']
})

export const listSchedules = createWsAction({
  type: LIST_SCHEDULES,
  messageType: 'attendance.schedule.list-schedules',
  selectorKey: 'schedules',
  fields: ['clientId']
})

export const listBlocks = createWsAction({
  type: LIST_BLOCKS,
  messageType: 'attendance.schedule.list-blocks',
  selectorKey: 'blocks',
  fields: ['clientId', 'scheduleId']
})

export const getContactAttendanceForSchedule = createWsAction({
  type: GET_CONTACT_ATTENDANCE_FOR_SCHEDULE,
  messageType: 'attendance.schedule.get-contact-attendance-for-schedule',
  selectorKey: 'contactAttendance',
  fields: ['clientId', 'scheduleId']
})

export const reportBlockAttendance = createWsAction({
  type: REPORT_BLOCK_ATTENDANCE,
  messageType: 'attendance.schedule.report-block-attendance',
  selectorKey: 'blockReport',
  fields: ['clientId', 'scheduleId', 'clientReference']
})

export const reportContactScheduleAttendance = createWsAction({
  type: REPORT_CONTACT_SCHEDULE_ATTENDANCE,
  messageType: 'attendance.schedule.report-contact-schedule-attendance',
  selectorKey: 'contactScheduleReport',
  fields: ['clientId']
})

export const overrideAttendance = createWsAction({
  type: OVERRIDE_ATTENDANCE,
  messageType: 'attendance.schedule.override-attendance',
  selectorKey: 'overrideAttendanceResult',
  fields: ['clientId', 'clientReference', 'scheduleId', 'blockId', 'status']
})

export const reportLowAttendanceByMetadata = createWsAction({
  type: REPORT_LOW_ATTENDANCE_BY_METADATA,
  messageType: 'attendance.schedule.report-low-attendance-by-metadata',
  selectorKey: 'lowAttendanceReport',
  fields: ['clientId', 'metadataKey', 'metadataValue', 'startTime', 'endTime', 'attendanceThreshold'],
  defaultParams: {
    metadataKey: 'programme',
    attendanceThreshold: '0.90'
  }
})

export default null
