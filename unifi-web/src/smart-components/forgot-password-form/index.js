import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'
import { Field, reduxForm } from 'redux-form'

import * as ROUTES from 'utils/routes'
import { Alert, Button } from 'elements'
import { TextField } from 'components'
import './index.scss'

const COMPONENT_CSS_CLASS = 'forgot-password-form'
const bemE = (suffix) => `${COMPONENT_CSS_CLASS}__${suffix}`
const isRequired = value => (value ? undefined : 'Username is required.')

class ForgotPasswordForm extends Component {
  static propTypes = {
    history: PropTypes.object.isRequired,
    onSubmit: PropTypes.func,
  }

  handleCancel = () => {
    const { history } = this.props;
    history.goBack()
  }

  handleSignIn = () => {
    const { history } = this.props;
    history.push(ROUTES.LOGIN)
  }

  render() {
    const { error, handleSubmit, submitSucceeded } = this.props
    return (
      <form onSubmit={handleSubmit}>
        {error && <Alert message={error} type="error" />}
        {submitSucceeded ? (
          <Fragment>
            <p className={bemE('text-success')}>
              A password reset link has been sent to the email address on file. If you do not receive it in
              10 to 15 minutes, please make sure you entered the correct username.
            </p>
            <div>
              <Button type="default" onClick={this.handleSignIn}>Back to Sign in</Button>
            </div>
          </Fragment>
        ) : (
          <Fragment>
            <p className={bemE('text')}>
              Please enter your username and we will send you an link to the email address on record to
              reset your password.
            </p>
            <Field
              name="username"
              label="Username"
              htmlType="usernmae"
              id="username"
              validate={[isRequired]}
              component={TextField}
            />
            <div>
              <Button htmlType="submit" type="primary">Submit</Button>{' '}
              <Button type="default" onClick={this.handleCancel}>Cancel</Button>
            </div>
          </Fragment>
        )}
      </form>
    )
  }
}

export default reduxForm({
  form: 'forgotPassword'
})(ForgotPasswordForm)
