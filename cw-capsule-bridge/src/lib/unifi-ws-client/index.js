import WSPackage from './ws/web-socket-package'
import WSProtocol from './ws/web-socket-protocol'

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
    const pack = new WSPackage({
      ...versions,
      ...content
    })
    return super.subscribe(pack.content, callback)
  }
}

export default UnifiWsClient
