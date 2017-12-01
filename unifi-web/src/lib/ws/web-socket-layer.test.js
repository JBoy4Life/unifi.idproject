import { Server } from 'mock-socket'

import WSLayer from './web-socket-layer'

test('WSLayer', () => {
  it('should intantiate without connecting', () => {
    const instance = new WSLayer()
    expect(instance).toBeInstanceOf(WSLayer)
  })

  it('should connect to sockets server', async () => {
    const mockServer = new Server('ws://localhost:8080')
    const connectionSpy = jest.fn()
    mockServer.on('connection', connectionSpy)
    const instance = new WSLayer('ws://localhost:8080')
    await instance.connect()
    await mockServer.stop()

    expect(connectionSpy).toHaveBeenCalledTime(1)
  })
})
