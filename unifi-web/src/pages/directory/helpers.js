import { base64EncodeArrayBuffer } from 'utils/helpers'

export const getImageData = data =>
  data instanceof ArrayBuffer
  ? process.env.REACT_APP_WS_MESSAGE_FORMAT === 'json'
    ? base64EncodeArrayBuffer(data)
    : data
  : undefined

export default null
