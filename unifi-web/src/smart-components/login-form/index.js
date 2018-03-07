import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Field, reduxForm } from 'redux-form'
import { Link } from 'react-router-dom'

import { TextField /* CheckboxField */ } from 'components'
import { Alert, Button, SubmitRow } from 'elements'
            
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
          extra={<Link to="/forgot-password">I've forgotten my password</Link>}
        />
        {/* <Field
          name="rememberme"
          label="Remember me"
          id="remember"
          component={CheckboxField}
        /> */}
        <SubmitRow>
          <Button htmlType="submit" type="primary" wide>Enter</Button>
        </SubmitRow>
      </form>
    )
  }
}

export default reduxForm({
  form: 'login',
  validate,
})(LoginForm)
