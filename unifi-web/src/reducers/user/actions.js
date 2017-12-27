import { USER_LOGOUT, USER_SET } from './types'

export const loginRequest = ({ username, password, remember }) => {
  console.log('should request login via websocket', username, password, remember)
  return {
    type: USER_SET,
    currentUser: username,
  }
}

export const logoutRequest = () => ({
  type: USER_LOGOUT,
})

export default null
