import { WSPackage } from '../../lib/ws'

import {
  OPERATORS_LIST_FETCH,
  OPERATOR_UPDATE
} from './types'


export const listOperators = ({ clientId, filter }) => {
  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion: '1.0.0',
    messageType: 'core.operator.list-operators',
    payload: { clientId, filter }
  })

  return {
    type: OPERATORS_LIST_FETCH,
    socketRequest: pack.content
  }
}


export const updateOperator = ({ clientId, name, email, active }) => {

  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion: '1.0.0',
    messageType: 'core.operator.edit-operator',
    payload: {
      clientId,
      name,
      email,
      active
    }
  });

  return {
    type: OPERATOR_UPDATE,
    socketRequest: pack.content
  };
}

export default null
