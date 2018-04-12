import createWsAction from 'utils/create-ws-action'

// ------------------------------------
// Action Types
// ------------------------------------
export const OPERATOR_LIST = 'unifi.OPERATOR_LIST'
export const OPERATOR_UPDATE = 'unifi.OPERATOR_UPDATE'
export const OPERATOR_GET = 'unifi.OPERATOR_GET'
export const OPERATOR_REGISTER = 'unifi.OPERATOR_REGISTER'

// ------------------------------------
// Action Creators
// ------------------------------------
export const listOperators = createWsAction({
  type: OPERATOR_LIST,
  messageType: 'core.operator.list-operators',
  selectorKey: 'operatorList',
  fields: ['clientId', 'filter']
})

export const getOperator = createWsAction({
  type: OPERATOR_GET,
  messageType: 'core.operator.get-operator',
  selectorKey: 'operatorDetails',
  fields: ['clientId', 'username']
})

export const updateOperator = createWsAction({
  type: OPERATOR_UPDATE,
  messageType: 'core.operator.edit-operator',
  selectorKey: 'operatorDetails',
  fields: ['clientId', 'username', 'changes']
})

export const registerOperator = createWsAction({
  type: OPERATOR_REGISTER,
  messageType: 'core.operator.register-operator',
  selectorKey: 'operatorDetail',
  fields: ['clientId', 'name', 'username', 'email', 'invite'],
  defaultParams: {
    invite: true
  }
})

export default null
