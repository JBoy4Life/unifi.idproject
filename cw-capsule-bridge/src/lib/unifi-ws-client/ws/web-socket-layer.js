/*
  eslint-disable
  no-param-reassign
 */
import msgpack from 'msgpack-lite'
import WebSocket from 'ws';

import { base64EncodeUint8Array } from './utils'

const binarryCodec = msgpack.createCodec({ binarraybuffer: true, preset: true })

export default class WebSocketLayer {
  constructor(wsConnectionURL, type = 'json') {
    this.wsConnectionURL = wsConnectionURL
    this.socket = null
    this.correlations = {}
    this.type = type // should be one of ['json', 'msgpack']
  }

  connect() {
    this.isConnected = false
    this.connected = new Promise((resolve, reject) => {
      this.socket = new WebSocket(this.wsConnectionURL)
      this.socket.binaryType = 'arraybuffer'
      this.once('error', reject)
      this.once('open', () => {
        this.isConnected = true
        resolve()
      })
    })

    return this.connected
  }

  close() {
    this.socket.close()
  }

  listen(callback, parse = true, message = 'message') {
    let handler = callback
    if (parse) {
      handler = (event) => {
        // console.log('listen', event.data)
        if (typeof event.data === 'string') {
          callback(event, JSON.parse(event.data))
        } else {
          this.decodeJSONFromBlob(event.data)
            .then((content) => {
              callback(event, content)
            })
        }
      }
    }
    this.socket.addEventListener(message, handler)
    return handler
  }

  unlisten(callback, message = 'message') {
    this.socket.removeEventListener(message, callback)
  }

  encodeMessage(json) {
    return msgpack.encode(json, { codec: binarryCodec })
  }

  base64EncodeUint8ArraysRecursively(obj) {
    const traverse = (obj) => {
      for (let key in obj) {
        if (obj[key] instanceof Uint8Array) {
          obj[key] = base64EncodeUint8Array(obj[key])
        }
        if (typeof obj[key] === 'object' && obj[key] && Object.keys(obj[key]).length > 0) {
          traverse(obj[key])
        }
      }
    }
    traverse(obj)
    return obj
  }

  decodeJSONFromBlob(response) {
    if (typeof Blob !== 'undefined' && response instanceof Blob) {
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
        reader.readAsArrayBuffer(response)
      })
    } else if (typeof Buffer !== 'undefined' && response instanceof Buffer) {
      return Promise.resolve(this.base64EncodeUint8ArraysRecursively(msgpack.decode(new Uint8Array(response))))
    } else if (typeof ArrayBuffer !== 'undefined' && response instanceof ArrayBuffer) {
      return Promise.resolve(this.base64EncodeUint8ArraysRecursively(msgpack.decode(new Uint8Array(response))))
    }

    return Promise.reject(new Error({ message: 'Unsupported message type' }))
  }

  async send(content) {
    if (!this.isConnected) {
      await this.connected
    }
    if (this.type === 'json') {
      this.socket.send(JSON.stringify(content));
    } else if (this.type === 'msgpack') {
      this.socket.send(this.encodeMessage(content));
    } else {
      throw 'Unsupported message format.';
    }
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
