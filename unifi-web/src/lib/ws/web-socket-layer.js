/*
  eslint-disable
  no-param-reassign
 */
import msgpack from 'msgpack-lite'

export default class WebSocketLayer {
  constructor(wsConnectionURL) {
    this.wsConnectionURL = wsConnectionURL
    this.socket = null
    this.corelations = {}
  }

  connect() {
    return new Promise((resolve, reject) => {
      this.socket = new WebSocket(this.wsConnectionURL)
      this.once('error', reject)
      this.once('open', resolve)
    })
  }

  close() {

  }

  listen(callback, parse = true) {
    let handler = callback
    if (parse) {
      handler = (event) => {
        this.decodeJSONFromBlob(event.data)
          .then((content) => {
            callback(event, content)
          })
      }
    }
    this.socket.addEventListener('message', handler)
    return handler
  }

  unlisten(callback) {
    this.socket.removeEventListener('message', callback)
  }

  encodeJSON(json) {
    return msgpack.encode(json)
  }

  decodeJSONFromBlob(blob) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader()
      reader.addEventListener('load', () => {
        resolve(msgpack.decode(new Uint8Array(reader.result, 0, reader.result.byteLength)))
      })
      reader.addEventListener('error', () => {
        reject(reader.error)
      })
      reader.addEventListener('abort', () => {
        reject(reader.error)
      })
      reader.readAsArrayBuffer(blob)
    })
  }

  send(message) {
    if (typeof message !== 'object') {
      throw new Error('Only JSON objects are supported for sending via socket layer')
    }

    this.socket.send(this.encodeJSON(message.content))
  }

  sendEncoded(message) {
    this.socket.send(message)
  }

  once(messageType, callback) {
    this.socket.addEventListener(messageType, (event) => {
      callback(event)
      this.socket.removeEventListener(messageType, callback)
    })
  }
}
