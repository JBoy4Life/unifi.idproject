import createAction from 'utils/create-ws-action'

import { DETECTABLES_LIST_FETCH } from './types'

export const listDetectables = createAction({
  type: DETECTABLES_LIST_FETCH,
  messageType: 'core.detectable.list-detectables',
  fields: ['clientId', 'filter', 'with'],
  defaultParams: {
    with: ['assignment']
  }
})

export default null
