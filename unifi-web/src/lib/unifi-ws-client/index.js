import WSPackage from './ws/web-socket-package'
import WSProtocol from './ws/web-socket-protocol'
import { addCorrelationId, removeCorrelationId } from './utils/helpers'

const versions = {
  protocolVersion: '1.0.0',
  releaseVersion: '1.0.0'
}

class UnifiWsClient extends WSProtocol {
  request(content, callback) {
    const pack = new WSPackage({
      ...versions,
      ...content
    })
    return super.request(pack.content).then(res => {
      callback && callback(res)
      return res
    })
  }

  subscribe(content, callback) {
    const component = content.component
    if (content.component)
      delete content.component

    const pack = new WSPackage({
      ...versions,
      ...content
    })
    addCorrelationId(pack.content.correlationId, component)
    return super.subscribe(pack.content, callback)
  }

  unsubscribe(content, callback) {
    removeCorrelationId(content.component, content.correlationId)
    delete content.component
    const pack = {
      ...versions,
      ...content
    }
    return super.unsubscribe(pack, callback)
  }
}

export default UnifiWsClient
