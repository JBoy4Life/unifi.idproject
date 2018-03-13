import { SubmissionError } from 'redux-form'
import tr from 'config/string-resources'

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
    const { payload } = res
    const fieldErrors = payload.errors ? payload.errors.reduce((acc, error) => {
      acc[error.field] = tr(error.issue)
      return acc
    }, {}) : {}
    throw new SubmissionError({
      _error: payload.message,
      ...fieldErrors
    })
  })
}

export default formSubmit
