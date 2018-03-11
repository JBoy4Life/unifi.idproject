import {
  OPERATORS_LIST_FETCH,
  OPERATOR_UPDATE
} from './types'

const initialState = {
  operatorList: []
}

const reducer = (state = initialState, action = {}) => {
  switch (action.type) {
    case `${OPERATORS_LIST_FETCH}_FULFILLED`:
      return {
        ...state,
        operatorList: action.payload.payload
      }
    default:
      return state
  }
}

export default reducer
