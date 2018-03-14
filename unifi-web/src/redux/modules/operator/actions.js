import createAction from 'utils/create-ws-action'

import {
  OPERATOR_LIST,
  OPERATOR_GET,
  OPERATOR_UPDATE,
  OPERATOR_REGISTER
} from './types'

export const listOperators = createAction({
  type: OPERATOR_LIST,
  messageType: 'core.operator.list-operators',
  fields: ['clientId', 'filter']
})

export const getOperator = createAction({
  type: OPERATOR_GET,
  messageType: 'core.operator.get-operator',
  fields: ['clientId', 'username']
})

export const updateOperator = createAction({
  type: OPERATOR_UPDATE,
  messageType: 'core.operator.edit-operator',
  fields: ['clientId', 'username', 'changes']
})

export const registerOperator = createAction({
  type: OPERATOR_REGISTER,
  messageType: 'core.operator.register-operator',
  fields: ['clientId', 'name', 'username', 'email', 'invite'],
  defaultParams: {
    invite: true
  }
})

export default null
