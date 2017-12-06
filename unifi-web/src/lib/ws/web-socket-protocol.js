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
    const { correlationId } = content
    const corelation = this.corelations[correlationId]
    if (corelation) {
      if (corelation.callback) {
        corelation.callback(content)
      } else {
        corelation.resolve(content)
        this.corelations[correlationId].handled = true
      }
    }
  }

  request(content) {
    console.log('request data', content)
    if (this.corelations[content.correlationId]) {
      return this.corelations[content.correlationId].resource
    }

    const resource = new Promise((resolve, reject) => {
      this.corelations[content.correlationId] = {
        resolve, reject, content,
      }
      this.ws.send(content)
    })

    this.corelations[content.correlationId].resource = resource
    return resource
  }

  subscribe(content, callback) {
    this.corelations[content.correlationId] = {
      callback, content,
    }
    this.ws.send(content)
  }
}
