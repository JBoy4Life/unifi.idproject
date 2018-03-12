import {
  OPERATOR_LIST,
  OPERATOR_GET,
  OPERATOR_UPDATE
} from './types'
import { API_PENDING, API_SUCCESS, API_FAIL } from 'redux/api/request'

const initialState = {
  operatorList: [],
  operatorListStatus: 'INIT',
  operatorDetails: null,
  operatorDetailsStatus: 'INIT'
}

const reducer = (state = initialState, action = {}) => {
  switch (action.type) {
    case `${OPERATOR_LIST}_PENDING`:
      return {
        ...state,
        operatorListStatus: API_PENDING
      }

    case `${OPERATOR_LIST}_FULFILLED`:
      return {
        ...state,
        operatorList: action.payload.payload,
        operatorListStatus: API_SUCCESS
      }

    case `${OPERATOR_LIST}_REJECTED`:
      return {
        ...state,
        operatorListStatus: API_FAIL
      }

    case `${OPERATOR_GET}_PENDING`:
      return {
        ...state,
        operatorDetailsStatus: API_PENDING
      }

    case `${OPERATOR_GET}_FULFILLED`:
      return {
        ...state,
        operatorDetails: action.payload.payload,
        operatorDetailsStatus: action.payload.payload ? API_SUCCESS : API_FAIL
      }

    case `${OPERATOR_GET}_REJECTED`:
      return {
        ...state,
        operatorDetailsStatus: API_REJECTED
      }

    default:
      return state
  }
}

export default reducer
