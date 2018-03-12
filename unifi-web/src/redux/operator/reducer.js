import {
  OPERATOR_LIST,
  OPERATOR_GET,
  OPERATOR_UPDATE
} from './types'

const initialState = {
  operatorList: [],
  operatorDetails: null
}

const reducer = (state = initialState, action = {}) => {
  switch (action.type) {
    case `${OPERATOR_LIST}_FULFILLED`:
      return {
        ...state,
        operatorList: action.payload.payload
      }
    case `${OPERATOR_GET}_FULFILLED`:
      return {
        ...state,
        operatorDetails: action.payload.payload
      }
    default:
      return state
  }
}

export default reducer
