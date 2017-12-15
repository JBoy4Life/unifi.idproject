import React from 'react'
import { Provider } from 'react-redux'
import { configureStore } from '../../src/main/store'


const store = configureStore()

export default function reduxProvider(storyFn) {
  return (
    <Provider store={store}>
      {storyFn()}
    </Provider>
  )
}
