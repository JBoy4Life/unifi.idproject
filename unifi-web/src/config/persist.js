import storage from 'redux-persist/es/storage'

const persistConfig = {
  key: 'root',
  storage,
  whitelist: ['user'],
}

export default persistConfig
