import { SubmissionError } from 'redux-form'

export const formSubmit = (actionCreator, payload) => {
  return (new Promise((resolve, reject) => {
    actionCreator({
      ...payload,
      formSubmit: {
        onSuccess: resolve,
        onFail: reject
      }
    })
  })).catch(res => {
    throw new SubmissionError({
      _error: res.payload.message
      // other field level errors will be added here.
    })
  })
}

export default formSubmit
