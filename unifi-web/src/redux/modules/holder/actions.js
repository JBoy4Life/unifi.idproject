import createAction from 'utils/create-ws-action'

import {
  HOLDER_GET_FETCH,
  HOLDERS_LIST_FETCH,
  PROGRAMMES_LIST_FETCH
} from './types'

export const listHolders = createAction({
  type: HOLDERS_LIST_FETCH,
  messageType: 'core.holder.list-holders',
  fields: ['clientId', 'with']
})

export const getHolder = createAction({
  type: HOLDER_GET_FETCH,
  messageType: 'core.holder.get-holder',
  fields: ['clientId', 'clientReference', 'with'],
  defaultParams: {
    with: ['metadata']
  }
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
