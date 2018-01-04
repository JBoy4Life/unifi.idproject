import {
  LIST_SCHEDULE_STATS,
  LIST_BLOCKS,
  GET_CONTACT_ATTENDANCE_FOR_SCHEDULE,
  REPORT_BLOCK_ATTENDANCE
} from './types'

const initialState = {
  clients: [],
};

const reducer = (state = initialState, action = {}) => {
  if (action.payload &&
      action.payload.messageType &&
      action.payload.messageType.startsWith("core.error")) {
    // Fail silently.
    return state;
  }
  switch (action.type) {
    case `${LIST_SCHEDULE_STATS}_FULFILLED`:
      return {
        ...state,
        scheduleStats: action.payload.payload,
      };
    case `${LIST_BLOCKS}_FULFILLED`:
      return {
        ...state,
        blocks: action.payload.payload
      };
    case `${GET_CONTACT_ATTENDANCE_FOR_SCHEDULE}_FULFILLED`:
      return {
        ...state,
        contactAttendance: action.payload.payload
      };
    case `${REPORT_BLOCK_ATTENDANCE}_FULFILLED`:
      return {
        ...state,
        blockReport: action.payload.payload
      };
    default:
      return state;
  }
};

export default reducer
