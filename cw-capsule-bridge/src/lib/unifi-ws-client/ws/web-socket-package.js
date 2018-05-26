import uuid4 from 'uuid/v4';
import btoa from 'btoa';

export default class WebSocketPackage {
  constructor(props) {
    const uuid = uuid4();
    const base64uuid = btoa(uuid);

    this.content = {
      ...props,
      correlationId: base64uuid,
    };
  }
}
