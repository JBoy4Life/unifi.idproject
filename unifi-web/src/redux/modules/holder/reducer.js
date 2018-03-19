import {
  HOLDER_ADD,
  HOLDER_GET_FETCH,
  HOLDER_UPDATE,
  HOLDERS_LIST_FETCH,
  PROGRAMMES_LIST_FETCH
} from './types'

import { API_PENDING, API_FAIL, API_SUCCESS } from 'redux/api/request'

const initialState = {
  holdersList: [],
  holderDetails: null,
  holderDetailsStatus: 'INIT',
  programmesList: []
}

const reducer = (state = initialState, action = {}) => {
  switch (action.type) {
    case `${HOLDERS_LIST_FETCH}_FULFILLED`:
      return {
        ...state,
        holdersList: action.payload.payload
      }

    case `${HOLDER_GET_FETCH}_PENDING`:
      return {
        ...state,
        holderDetails: null,
        holderDetailsStatus: API_PENDING
      }
    case `${HOLDER_GET_FETCH}_FULFILLED`:
      return {
        ...state,
        holderDetails: action.payload.payload,
        holderDetailsStatus: API_SUCCESS
      }
    case `${HOLDER_GET_FETCH}_REJECTED`:
      return {
        ...state,
        holderDetailsStatus: API_REJECTED
      }

    case `${HOLDER_ADD}_FULFILLED`:
      return {
        ...state,
        holderDetails: action.payload.payload,
        holderDetailsStatus: API_SUCCESS
      }

    case `${HOLDER_UPDATE}_FULFILLED`:
      return {
        ...state,
        holderDetails: action.payload.payload,
        holderDetailsStatus: API_SUCCESS
      }

    case `${PROGRAMMES_LIST_FETCH}_FULFILLED`:
      return {
        ...state,
        programmesList: action.payload.payload.filter(val => Boolean(val))
      }

    default:
      return state
  }
}

export default reducer
