import React from 'react'
import { Provider } from 'react-redux'
import store from '../../src/main/store'

export default function reduxProvider(storyFn) {
  console.log('adding store', store)
  return (
    <Provider store={store}>
      {storyFn()}
    </Provider>
  )
}
