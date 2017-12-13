// reducers will be exported from here
import { reducer as form } from 'redux-form'

import { reducer as user } from './user'
import { reducer as clients } from './clients'

export default {
  form,
  user,
  clients,
}
