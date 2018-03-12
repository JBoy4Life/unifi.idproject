import { WSPackage } from '../../lib/ws'

import {
  OPERATOR_LIST,
  OPERATOR_GET,
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
    type: OPERATOR_LIST,
    socketRequest: pack.content
  }
}


export const getOperator = ({ clientId, username }) => {
  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion: '1.0.0',
    messageType: 'core.operator.get-operator',
    payload: { clientId, username }
  })

  return {
    type: OPERATOR_GET,
    socketRequest: pack.content
  }
}

export const updateOperator = ({ clientId, username, changes, formSubmit }) => {

  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion: '1.0.0',
    messageType: 'core.operator.edit-operator',
    payload: {
      clientId,
      username,
      changes
    }
  });

  return {
    type: OPERATOR_UPDATE,
    socketRequest: pack.content,
    formSubmit
  };
}

export default null
