import { createStore, applyMiddleware, compose } from 'redux'
import promiseMiddleware from 'redux-promise-middleware'
import storage from 'redux-persist/es/storage'
import { persistStore, persistCombineReducers } from 'redux-persist'
import { composeWithDevTools } from 'redux-devtools-extension'

import { socketApiMiddleware } from '../middleware'
import reducers from '../reducers'
import * as userActions from "../reducers/user/actions";

const config = {
  key: 'root',
  storage,
  whitelist: ['user'],
};

export const configureStore = (wsClient) => {

  // Create a history of your choosing (we're using a browser history in this case)
  const isDevelopment = process.env.NODE_ENV === 'development';

  const middlewares = [
    promiseMiddleware(),
    socketApiMiddleware(wsClient),
  ];

  const currentUser = localStorage.getItem('unifi-current-user') ?
    JSON.parse(localStorage.getItem('unifi-current-user')) :
    {};

  // Add the reducer to your store on the `router` key
  // Also apply our middleware for navigating
  const store = createStore(
    persistCombineReducers(config, reducers),
    isDevelopment ?
      composeWithDevTools(applyMiddleware(...middlewares)) :
      compose(applyMiddleware(...middlewares)),
  );

  const persistor = persistStore(store);

  // Reauthenticate if we have a session.
  if (currentUser.payload.token) {
    const action = userActions.reauthenticateRequest(currentUser.payload.token);
    store.dispatch(action);
  }

  store.subscribe(() => {
    // Ensure that user sessions go to local storage.
    const currentUser = store.getState().currentUser;
    if (currentUser) {
      localStorage.setItem('unifi-current-user', JSON.stringify(currentUser));
    }
  });

  return { store, persistor }

};
