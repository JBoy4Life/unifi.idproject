import createAction from 'utils/create-ws-action'

import {
  LIST_SCHEDULE_STATS,
  LIST_SCHEDULES,
  LIST_BLOCKS,
  GET_CONTACT_ATTENDANCE_FOR_SCHEDULE,
  REPORT_BLOCK_ATTENDANCE,
  REPORT_CONTACT_SCHEDULE_ATTENDANCE,
  REPORT_LOW_ATTENDANCE_BY_METADATA,
  OVERRIDE_ATTENDANCE
} from './types'

export const listScheduleStats = createAction({
  type: LIST_SCHEDULE_STATS,
  messageType: 'attendance.schedule.list-schedule-stats',
  fields: ['clientId']
})

export const listSchedules = createAction({
  type: LIST_SCHEDULES,
  messageType: 'attendance.schedule.list-schedules',
  fields: ['clientId']
})

export const listBlocks = createAction({
  type: LIST_BLOCKS,
  messageType: 'attendance.schedule.list-blocks',
  fields: ['clientId', 'scheduleId']
})

export const getContactAttendanceForSchedule = createAction({
  type: GET_CONTACT_ATTENDANCE_FOR_SCHEDULE,
  messageType: 'attendance.schedule.get-contact-attendance-for-schedule',
  fields: ['clientId', 'scheduleId']
})

export const reportBlockAttendance = createAction({
  type: REPORT_BLOCK_ATTENDANCE,
  messageType: 'attendance.schedule.report-block-attendance',
  fields: ['clientId', 'scheduleId', 'clientReference']
})

export const reportContactScheduleAttendance = createAction({
  type: REPORT_CONTACT_SCHEDULE_ATTENDANCE,
  messageType: 'attendance.schedule.report-contact-schedule-attendance',
  fields: ['clientId']
})

export const overrideAttendance = createAction({
  type: OVERRIDE_ATTENDANCE,
  messageType: 'attendance.schedule.override-attendance',
  fields: ['clientId', 'clientReference', 'scheduleId', 'blockId', 'status']
})

export const reportLowAttendanceByMetadata = createAction({
  type: REPORT_LOW_ATTENDANCE_BY_METADATA,
  messageType: 'attendance.schedule.report-low-attendance-by-metadata',
  fields: ['clientId', 'metadataKey', 'metadataValue', 'startTime', 'endTime', 'attendanceThreshold'],
  defaultParams: {
    metadataKey: 'programme',
    attendanceThreshold: '0.90'
  }
})

export default null
