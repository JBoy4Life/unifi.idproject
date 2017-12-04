import WebSocketLayer from './web-socket-layer'
import { sleep } from '../../utils/helpers'

export default class WebSocketProtocol {
  constructor({
    url,
    WebSocketLayerClass = WebSocketLayer,
    reconnectionAttempts = 100,
    reconnectionDelay = 1000,
  }) {
    this.corelations = []
    this.ws = new WebSocketLayerClass(url)
    this.reconnectionAttempts = reconnectionAttempts
    this.reconnectionDelay = reconnectionDelay
    this.attemptCount = 0
  }

  handleConnectionError = (err) => {
    // console.log('handleConnectionError', err)
    if (err.target.readyState === 3) {
      this.handleDisconnect()
    }
  }

  handleDisconnect = async () => {
    if (this.reconnectionAttempts > this.attemptCount) {
      this.attemptCount += 1
      await sleep(this.reconnectionDelay)
      await this.connect()
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
    const { corelationId } = content
    const corelation = this.corelations[corelationId]
    if (corelation) {
      if (corelation.callback) {
        corelation.callback(content)
      } else {
        corelation.resolve(content)
        this.corelations[corelationId].handled = true
      }
    }
  }

  request(content) {
    if (this.corelations[content.corelationId]) {
      return this.corelations[content.corelationId].resource
    }

    const resource = new Promise((resolve, reject) => {
      this.corelations[content.corelationId] = {
        resolve, reject, content,
      }
      this.ws.send(content)
    })

    this.corelations[content.corelationId].resource = resource
    return resource
  }

  subscribe(content, callback) {
    this.corelations[content.corelationId] = {
      callback, content,
    }
    this.ws.send(content)
  }
}
