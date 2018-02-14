import createHistory from 'history/createBrowserHistory'
import promiseMiddleware from 'redux-promise-middleware'
import { composeWithDevTools } from 'redux-devtools-extension'
import { createStore, applyMiddleware, compose } from 'redux'
import { persistStore, persistCombineReducers } from 'redux-persist'

import { authMiddleware, formSubmitMiddleware, socketApiMiddleware } from './middleware'
import reducers from 'redux/reducers'
import persistConfig from 'config/persist'

export const history = createHistory()

export const configureStore = (wsClient) => {

  // Create a history of your choosing (we're using a browser history in this case)
  const isDevelopment = process.env.NODE_ENV === 'development'

  const middlewares = [
    promiseMiddleware(),
    socketApiMiddleware(wsClient),
    authMiddleware,
    formSubmitMiddleware,
  ]

  // Add the reducer to your store on the `router` key
  // Also apply our middleware for navigating
  const store = createStore(
    persistCombineReducers(persistConfig, reducers),
    isDevelopment ?
      composeWithDevTools(applyMiddleware(...middlewares)) :
      compose(applyMiddleware(...middlewares)),
  )

  const persistor = persistStore(store)

  return { store, persistor }
}
