import {
  CLIENT_CREATE,
  CLIENT_GET_FETCH,
  CLIENT_LIST_FETCH,
} from './types'

import { API_SUCCESS, API_FAIL, API_PENDING } from 'redux/api/request'

const initialState = {
  clients: [],
  currentClient: null,
  clientCreateStatus: 'INIT',
  clientListStatus: 'INIT',
  clientGetStatus: 'INIT'
}

const reducer = (state = initialState, action = {}) => {
  // console.log(action.type, action)
  switch (action.type) {
    case `${CLIENT_CREATE}_PENDING`:
      return {
        ...state,
        clientCreateStatus: API_PENDING
      }

    case `${CLIENT_CREATE}_REJECTED`:
      return {
        ...state,
        clientCreateStatus: API_FAIL
      }

    case `${CLIENT_CREATE}_FULFILLED`:
      return {
        ...state,
        clientCreateStatus: API_SUCCESS,
      }

    case `${CLIENT_LIST_FETCH}_PENDING`:
      return {
        ...state,
        clientListStatus: API_PENDING
      }

    case `${CLIENT_LIST_FETCH}_FULFILLED`:
      return {
        ...state,
        clients: action.payload.payload,
        clientListStatus: API_SUCCESS
      }

    case `${CLIENT_LIST_FETCH}_REJECTED`:
      return {
        ...state,
        clients: [],
        clientListStatus: API_FAIL
      }

    case `${CLIENT_GET_FETCH}_PENDING`:
      return {
        ...state,
        clientGetStatus: API_PENDING
      }

    case `${CLIENT_GET_FETCH}_FULFILLED`:
      return {
        ...state,
        currentClient: action.payload.payload,
        clientGetStatus: API_SUCCESS
      }

    case `${CLIENT_GET_FETCH}_REJECTED`:
      return {
        ...state,
        currentClient: null,
        clientGetStatus: API_FAIL
      }

    default:
      return state
  }
}

export default reducer
