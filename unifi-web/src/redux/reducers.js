import { reducer as form } from 'redux-form'

import model from './modules/model'
import websocket from './modules/websocket'
import user from './modules/user'

export default {
  // Add reducers here
  model,
  websocket,
  form,
  user
}
