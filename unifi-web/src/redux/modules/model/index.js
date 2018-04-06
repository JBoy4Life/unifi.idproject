import set from 'lodash/set'
import unset from 'lodash/unset'
import { handleActions } from 'redux-actions'

import {
  API_PENDING,
  API_FAIL,
  API_SUCCESS,
  API_SUBSCRIBE_UPDATE
} from 'redux/api/constants'

export default handleActions({
  [API_SUCCESS]: (state, { payload }) =>
    set(state, payload.selectorKey, payload.data),

  [API_FAIL]: (state, { payload }) => {
    unset(state, payload.selectorKey)
    return state
  },

  [API_SUBSCRIBE_UPDATE]: (state, { payload }) => ({
    ...set(state, payload.selectorKey, payload.data)
  }),
}, {});
