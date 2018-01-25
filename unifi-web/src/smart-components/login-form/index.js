import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Field, reduxForm } from 'redux-form'

import { TextField /* CheckboxField */ } from 'components'
import { Alert, Button } from 'elements'
            
import validate from './validate'

class LoginForm extends Component {
  static propTypes = {
    onSubmit: PropTypes.func,
  }

  render() {
    const { error, handleSubmit } = this.props
    return (
      <form onSubmit={handleSubmit}>
        {error && <Alert message={error} type="error" />}
        <Field
          name="username"
          label="Username"
          id="username"
          component={TextField}
        />
        <Field
          name="password"
          label="Password"
          htmlType="password"
          id="password"
          component={TextField}
        />
        {/* <Field
          name="rememberme"
          label="Remember me"
          id="remember"
          component={CheckboxField}
        /> */}
        <Button htmlType="submit" type="primary">Log In</Button>
      </form>
    )
  }
}

export default reduxForm({
  form: 'login',
  validate,
})(LoginForm)
