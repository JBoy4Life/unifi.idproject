import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Field, reduxForm } from 'redux-form'
import { TextField /* CheckboxField */ } from '../../components'
import { Button } from '../../elements'

import validate from './validate'

class LoginForm extends Component {
  static propTypes = {
    onSubmit: PropTypes.func,
  }

  render() {
    const { handleSubmit, onSubmit } = this.props

    return (
      <form onSubmit={handleSubmit(onSubmit)}>
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
        <Button htmlType="submit" type="primary">Login</Button>
      </form>
    )
  }
}

export default reduxForm({
  form: 'login',
  validate,
})(LoginForm)
