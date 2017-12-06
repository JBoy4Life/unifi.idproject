import uuid4 from 'uuid/v4'

export default class WebSocketPackage {
  constructor(props) {
    const { payload = null } = props || {}
    this.content = {
      ...props,
      correlationId: btoa(uuid4()),
      payload,
    }
  }
}
