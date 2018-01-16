// reducers will be exported from here
import { reducer as form } from 'redux-form'

import { reducer as attendance } from './attendance'
import { reducer as clients } from './clients'
import { reducer as liveZones } from './zones'
import { reducer as user } from './user'

export default {
  attendance,
  clients,
  form,
  liveZones,
  user
}
