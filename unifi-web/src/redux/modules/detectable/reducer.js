import unionBy from 'lodash/unionBy'

import { DETECTABLES_LIST_FETCH } from './types'

const initialState = {
  detectablesList: []
}

const reducer = (state = initialState, action = {}) => {
  switch (action.type) {
    case `${DETECTABLES_LIST_FETCH}_FULFILLED`:
      return {
        ...state,
        detectablesList: action.payload.payload
      }
    default:
      return state
  }
}

export default reducer
