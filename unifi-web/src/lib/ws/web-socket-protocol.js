import WebSocketLayer from './web-socket-layer'

export default class WebSocketProtocol {
  constructor({
    url,
    WebSocketLayerClass = WebSocketLayer,
  }) {
    this.corelations = []
    this.ws = new WebSocketLayerClass(url)
  }

  connect() {
    return this.ws.connect()
  }

  close() {
    return this.ws.close()
  }

  start() {
    this.messageHandlerID = this.ws.listen(this.handleMessage)
  }

  pause() {
    this.ws.unlisten(this.messageHandlerID)
    this.messageHandlerID = null
  }

  handleMessage = (event, content) => {
    console.log('handleMessage', content)
    const { corelationId } = content
    const corelation = this.corelations[corelationId]
    console.log(corelation)
    if (corelation) {
      if (corelation.callback) {
        corelation.callback(content)
      } else {
        corelation.resolve(content)
        this.corelations[corelationId].handled = true
      }
    }
  }

  request(pack) {
    const { content } = pack
    if (this.corelations[content.corelationId]) {
      console.warn('preventing duplicate package send')
      return this.corelations[content.corelationId].resource
    }

    const resource = new Promise((resolve, reject) => {
      this.corelations[content.corelationId] = {
        resolve, reject, content,
      }
      this.ws.send(pack)
    })

    this.corelations[content.corelationId].resource = resource
    return resource
  }

  subscribe(pack, callback) {
    this.corelations[pack.corelationId] = {
      callback, pack,
    }
    this.ws.send(pack)
  }
}
