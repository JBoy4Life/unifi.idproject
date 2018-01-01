import { WSPackage } from '../../lib/ws'

import { USER_LOGOUT, USER_LOGIN } from './types'

export const loginRequest = ({ username, password /* remember */ }) => {
  // console.log('should request login via websocket', username, password, remember)

  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion: '1.0.0',
    messageType: 'core.operator.auth-password',
    payload: { username, password, clientId: 'ucl-mgmt' },
  })

  console.log('USER_LOGIN', pack.content)

  return {
    socketRequest: pack.content,
    type: USER_LOGIN,
  }
}

export const logoutRequest = () => ({
  type: USER_LOGOUT,
})

export default null
