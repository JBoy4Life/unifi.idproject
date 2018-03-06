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

import { API_PENDING, API_SUCCESS, API_FAIL } from '../api/request'

const initialState = {
  scheduleStats: [],
  schedules: [],
  blocks: [],
  contactAttendance: {
    attendance: []
  },
  blockReport: [],
  contactScheduleReport: [],
  putAssignmentResult: {},
  overrideAttendanceResult: {},
  lowAttendanceReport: {},
  lowAttendanceReportStatus: 'INIT'
}

const reducer = (state = initialState, action = {}) => {
  if (action.payload &&
      action.payload.messageType &&
      action.payload.messageType.startsWith("core.error")) {
    // Fail silently.
    return state
  }
  switch (action.type) {
    case `${LIST_SCHEDULE_STATS}_FULFILLED`:
      return {
        ...state,
        scheduleStats: action.payload.payload,
      }
    case `${LIST_SCHEDULES}_FULFILLED`:
      return {
        ...state,
        schedules: action.payload.payload
      }
    case `${LIST_BLOCKS}_FULFILLED`:
      return {
        ...state,
        blocks: action.payload.payload
      }
    case `${GET_CONTACT_ATTENDANCE_FOR_SCHEDULE}_FULFILLED`:
      return {
        ...state,
        contactAttendance: action.payload.payload
      }
    case `${REPORT_BLOCK_ATTENDANCE}_FULFILLED`:
      return {
        ...state,
        blockReport: action.payload.payload
      }
    case `${REPORT_CONTACT_SCHEDULE_ATTENDANCE}_FULFILLED`:
      return {
        ...state,
        contactScheduleReport: action.payload.payload
      }
    case `${OVERRIDE_ATTENDANCE}_FULFILLED`:
      return {
        ...state,
        overrideAttendanceResult: action.payload
      }
    case `${REPORT_LOW_ATTENDANCE_BY_METADATA}_PENDING`:
      return {
        ...state,
        lowAttendanceReportStatus: API_PENDING
      }
    case `${REPORT_LOW_ATTENDANCE_BY_METADATA}_FULFILLED`:
      return {
        ...state,
        lowAttendanceReportStatus: API_SUCCESS,
        lowAttendanceReport: action.payload.payload
      }
    case `${REPORT_LOW_ATTENDANCE_BY_METADATA}_REJECTED`:
      return {
        ...state,
        lowAttendanceReportStatus: API_FAIL
      }
    default:
      return state
  }
}

export default reducer
