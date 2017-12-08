const WebSocket = require('ws');
const msgpack = require('msgpack-lite');
const wss = new WebSocket.Server({ port: 8080 });

const dummyMessageHandler = (message, ws) => {
  switch(message.messageType) {
    case 'beep': 
      setTimeout(() => {
        ws.send(
          msgpack.encode({
            correlationId: message.correlationId,
            payload: {
              message: "boop"
            }
          })
        )
      }, 2000)
      return;
    case 'core.client.register-client': 
      ws.send(
        msgpack.encode({
          correlationId: message.correlationId,
          payload: {
            message: "world"
          }
        })
      )
      return

    default:
      return
  }
}

wss.on('connection', function connection(ws) {
  ws.on('message', function incoming(message) {
    console.log('received: %s', typeof message, message);
    console.log('decoded', msgpack.decode(message))
    dummyMessageHandler(msgpack.decode(message), ws)
  });  
});