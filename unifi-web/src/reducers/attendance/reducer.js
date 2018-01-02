import {
  LIST_SCHEDULE_STATS,
  LIST_BLOCKS
} from './types'

const initialState = {
  clients: [],
}

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
    default:
      return state;
  }
};

export default reducer
