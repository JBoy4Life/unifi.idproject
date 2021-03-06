import createHistory from 'history/createBrowserHistory'
import thunk from 'redux-thunk';
import { composeWithDevTools } from 'redux-devtools-extension'
import { createStore, applyMiddleware, compose } from 'redux'
import { persistStore, persistCombineReducers } from 'redux-persist'

import persistConfig from 'config/persist'
import reducers from './reducers'
import { authMiddleware, formSubmitMiddleware, socketApiMiddleware } from './middleware'

export const history = createHistory()

export const configureStore = (wsClient) => {

  // Create a history of your choosing (we're using a browser history in this case)
  const isDevelopment = process.env.NODE_ENV === 'development'

  const middlewares = [
    thunk,
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
