import { createStore, applyMiddleware, compose } from 'redux'
import promiseMiddleware from 'redux-promise-middleware'
import storage from 'redux-persist/es/storage'
import { persistStore, persistCombineReducers } from 'redux-persist'
import { composeWithDevTools } from 'redux-devtools-extension'

import { socketApiMiddleware } from '../middleware'
import reducers from '../reducers'

const config = {
  key: 'root',
  storage,
  whitelist: ['user'],
}

export const configureStore = (wsClient) => {
  // Create a history of your choosing (we're using a browser history in this case)
  const isDevelopment = process.env.NODE_ENV === 'development'

  const middlewares = [
    promiseMiddleware(),
    socketApiMiddleware(wsClient),
  ]

  // Add the reducer to your store on the `router` key
  // Also apply our middleware for navigating
  const store = createStore(
    persistCombineReducers(config, reducers),
    isDevelopment ?
      composeWithDevTools(applyMiddleware(...middlewares)) :
      compose(applyMiddleware(...middlewares)),
  )

  const persistor = persistStore(store)

  return { store, persistor }
}

export default configureStore
