export const loginRequest = ({ username, password, remember }) => {
  console.log('should request login via websocket', username, password, remember)
  return {
    payload: Promise.resolve(),
    type: 'TEST',
  }
}

export default null
