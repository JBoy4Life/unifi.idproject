/*
 es-lint disable
 no-await-in-loop
*/

import { Server } from 'mock-socket'
import msgpack from 'msgpack-lite'
import WSProtocol from './web-socket-protocol'

const sleep = time => new Promise(resolve => (
  setTimeout(resolve, time)
))

describe('WSProtocol', () => {
  it('should intantiate without connecting', () => {
    const instance = new WSProtocol({ url: 'ws://localhost:8080' })
    expect(instance).toBeInstanceOf(WSProtocol)
  })

  it('should attempt to reconnect when connection droped by server', async () => {
    const mockServer = new Server('ws://localhost:8080')

    mockServer.on('connection', (ws) => {
      setTimeout(() => {
        ws.close()
      }, 100)
    })

    const instance = new WSProtocol({
      url: 'ws://localhost:8080',
      reconnectionAttempts: 3,
      reconnectionDelay: 100,
    })

    await instance.connect()
    await sleep(550)
    await instance.close()
    await mockServer.stop()
    expect(instance.attemptCount).toBe(3)
  })

  it('should attempt to connect when connection is not reachable', async () => {
    const instance = new WSProtocol({
      url: 'ws://localhost:8080',
      reconnectionAttempts: 3,
      reconnectionDelay: 100,
    })

    await instance.connect()
    await sleep(550)
    await instance.close()
    expect(instance.attemptCount).toBe(3)
  })


  it('should support request response type messages', async () => {
    const mockServer = new Server('ws://localhost:8080')
    const correlationId = '123456789'
    mockServer.on('connection', (ws) => {
      ws.on('message', () => {
        ws.send(msgpack.encode({
          correlationId,
          payload: {
            message: 'boop',
          },
        }))

        ws.close()
      })
    })

    const instance = new WSProtocol({
      url: 'ws://localhost:8080',
    })

    await instance.connect()
    instance.start()

    const response = await instance.request({
      correlationId,
      payload: null,
    })

    await instance.close()
    await mockServer.stop()

    expect(response).toHaveProperty('payload')
  })


  it('should support subscription response type messages', async () => {
    const mockServer = new Server('ws://localhost:8080')
    const correlationId = '123456789'
    mockServer.on('connection', (ws) => {
      ws.on('message', async () => {
        let i = 0
        await sleep(50)
        while (i < 3) {
          ws.send(msgpack.encode({
            correlationId,
            payload: {
              message: 'boop',
            },
          }))
          i += 1
        }
      })
    })

    const instance = new WSProtocol({
      url: 'ws://localhost:8080',
    })

    await instance.connect()
    instance.start()

    let responseCount = 0
    instance.subscribe({
      correlationId,
      payload: null,
    }, () => {
      responseCount += 1;
    });

    await sleep(200)
    await instance.close()
    await mockServer.stop()

    expect(responseCount).toBe(3)
  })
})
