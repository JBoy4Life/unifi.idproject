import uuid4 from 'uuid/v4'

export default class WebSocketPackage {
  constructor({
    type, protocolVersion = 1, apiVersion = 1, payload = null,
  }) {
    this.content = {
      apiVersion,
      protocolVersion,
      type,
      corelationId: btoa(uuid4()),
      payload,
    }
  }
}
