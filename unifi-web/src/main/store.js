import { createStore, applyMiddleware, compose } from 'redux'
import promiseMiddleware from 'redux-promise-middleware'
import storage from 'redux-persist/es/storage'
import { persistStore, persistCombineReducers } from 'redux-persist'
import { composeWithDevTools } from 'redux-devtools-extension'

import { authMiddleware, socketApiMiddleware } from '../middleware'
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
    authMiddleware
  ];

  // Add the reducer to your store on the `router` key
  // Also apply our middleware for navigating
  const store = createStore(
    persistCombineReducers(config, reducers),
    isDevelopment ?
      composeWithDevTools(applyMiddleware(...middlewares)) :
      compose(applyMiddleware(...middlewares)),
  );

  const persistor = persistStore(store);

  store.subscribe(() => {
    // Ensure that user sessions go to local storage.
    const newUser = store.getState().user.currentUser;
    if (newUser) {
      localStorage.setItem('unifi-current-user', JSON.stringify(newUser));
    } else if (newUser === null) {
      localStorage.removeItem('unifi-current-user');
    }
  });

  return { store, persistor }

};
