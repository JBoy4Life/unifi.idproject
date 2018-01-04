import { WSPackage } from '../../lib/ws';

import {
  USER_LOGOUT,
  USER_LOGIN,
  USER_REAUTHENTICATE
} from './types';

export const loginRequest = ({ username, password /* remember */ }) => {

  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion: '1.0.0',
    messageType: 'core.operator.auth-password',
    payload: { username, password, clientId: 'ucl-mgmt' },
  });

  return {
    socketRequest: pack.content,
    type: USER_LOGIN,
  }
};

export const reauthenticateRequest = (sessionToken) => {

  const pack = new WSPackage({
    protocolVersion: '1.0.0',
    releaseVersion:  '1.0.0',
    messageType: 'core.operator.auth-token',
    payload: {
      clientId: 'ucl-mgmt',
      sessionToken
    }
  });

  return {
    socketRequest: pack.content,
    type: USER_REAUTHENTICATE
  };

};

export const logoutRequest = () => ({
  type: USER_LOGOUT
});

export default null
