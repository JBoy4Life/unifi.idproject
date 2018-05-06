import WebSocketLayer from './web-socket-layer'
import { sleep } from './utils'

export default class WebSocketProtocol {
  constructor({
    url,
    WebSocketLayerClass = WebSocketLayer,
    reconnectionAttempts = 100,
    reconnectionDelay = 1000,
    type = 'json'
  }) {
    // console.log('WebSocketProtocol', url)
    this.correlations = []
    this.ws = new WebSocketLayerClass(url, type)
    this.reconnectionAttempts = reconnectionAttempts
    this.reconnectionDelay = reconnectionDelay
    this.attemptCount = 0
  }

  handleConnectionError = (err) => {
    console.log('handleConnectionError', err)
    if (err.target.readyState === 3) {
      this.handleDisconnect()
    }
  }

  handleDisconnect = async () => {
    if (this.reconnectionAttempts > this.attemptCount) {
      this.attemptCount += 1
      await sleep(this.reconnectionDelay)
      await this.connect()
      this.start()
    }
  }

  async connect() {
    try {
      await this.ws.connect()
      this.attemptCount = 0
      this.ws.once('close', this.handleDisconnect)
    } catch (err) {
      this.handleConnectionError(err)
    }
  }

  close() {
    this.pause()
    return this.ws.close()
  }

  start() {
    this.messageHandlerID = this.ws.listen(this.handleMessage)
  }

  pause() {
    if (this.messageHandlerID) {
      this.ws.unlisten(this.messageHandlerID)
      this.messageHandlerID = null
    }
  }

  handleMessage = (event, content) => {
    const { correlationId } = content
    const correlation = this.correlations[correlationId]
    if (correlation) {
      if (correlation.callback) {
        correlation.callback(content)
      } else {
        correlation.resolve(content)
        this.correlations[correlationId].handled = true
      }
    }
  }

  request(content, params) {
    // console.log('request data', content)
    if (this.correlations[content.correlationId]) {
      return this.correlations[content.correlationId].resource
    }

    const resource = new Promise((resolve, reject) => {
      this.correlations[content.correlationId] = {
        resolve, reject, content,
      }
      // console.log('sending content', content)
      this.ws.send(content, params)
    })

    this.correlations[content.correlationId].resource = resource
    return resource
  }

  subscribe(content, params, callback) {
    this.correlations[content.correlationId] = {
      callback, content,
    }
    this.ws.send(content, params)
  }
}
