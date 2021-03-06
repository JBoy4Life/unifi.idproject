import uuid4 from 'uuid/v4'
import { btoa } from './utils'
// function ab2str(buf) {
//   return String.fromCharCode.apply(null, new Uint8Array(buf))
// }

// function str2ab(str) {
//   const buf = new ArrayBuffer(str.length * 2) // 2 bytes for each char
//   const bufView = new Uint8Array(buf)
//   for (let i = 0, strLen = str.length; i < strLen; i += 1) {
//     bufView[i] = str.charCodeAt(i)
//   }
//   return buf
// }


export default class WebSocketPackage {
  constructor(props) {
    const uuid = uuid4()
    const base64uuid = btoa(uuid)

    this.content = {
      ...props,
      correlationId: base64uuid,
    }
  }
}
