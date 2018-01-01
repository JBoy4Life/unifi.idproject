import {
  LIST_SCHEDULE_STATS,
} from './types'

const initialState = {
  clients: [],
}

const reducer = (state = initialState, action = {}) => {
  switch (action.type) {
    case `${LIST_SCHEDULE_STATS}_FULFILLED`:
      if (action.payload.messageType.startsWith("core.error")) {
        // Fail silently for now.
        return state;
      } else {
        return {
          ...state,
          scheduleStats: action.payload.payload,
        };
      }

    default:
      return state
  }
};

export default reducer
