import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Field, reduxForm } from 'redux-form'

import { TextField /* CheckboxField */ } from 'components'
import { Alert, Button } from 'elements'

import validate from './validate'

class ResetPasswordForm extends Component {
  static propTypes = {
    onSubmit: PropTypes.func,
  }

  render() {
    const { error, handleSubmit } = this.props
    return (
      <form onSubmit={handleSubmit}>
        {error && <Alert message={error} type="error" />}
        <Field
          name="password"
          label="Password"
          htmlType="password"
          id="password"
          component={TextField}
        />
        <Field
          name="confirmPassword"
          label="Confirm Password"
          htmlType="password"
          id="confirmPassword"
          component={TextField}
        />
        <Button htmlType="submit" type="primary">Submit</Button>
      </form>
    )
  }
}

export default reduxForm({
  form: 'resetPassword',
  validate,
})(ResetPasswordForm)
