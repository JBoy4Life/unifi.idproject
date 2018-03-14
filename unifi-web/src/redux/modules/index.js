// reducers will be exported from here
import { reducer as form } from 'redux-form'

import { reducer as attendance } from './attendance'
import { reducer as client } from './client'
import { reducer as detectable } from './detectable'
import { reducer as holder } from './holder'
import { reducer as operator } from './operator'
import { reducer as site } from './site'
import { reducer as user } from './user'

export default {
  attendance,
  client,
  detectable,
  form,
  holder,
  operator,
  site,
  user
}
