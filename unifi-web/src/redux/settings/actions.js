import createAction from 'utils/create-ws-action'

import {
  PROGRAMMES_LIST_FETCH,
  SITES_LIST_FETCH
} from './types'

export const listSites = createAction({
  type: SITES_LIST_FETCH,
  messageType: 'core.site.list-sites',
  fields: ['clientId']
})

export const listProgrammes = createAction({
  type: PROGRAMMES_LIST_FETCH,
  messageType: 'core.holder.list-metadata-values',
  fields: ['clientId', 'metadataKey'],
  defaultParams: {
    metadataKey: 'programme'
  }
})

export default null
