import React, { Component } from 'react'
import get from 'lodash'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { Field, reduxForm } from 'redux-form'
import { withRouter } from 'react-router'

import * as ROUTES from 'utils/routes' 
import * as validate from './validate'
import { Alert, Button, Col, Row, SubmitRow } from 'elements'
import { SwitchField, TextField } from 'components'

class OperatorForm extends Component {
  static propTypes = {
    handleSubmit: PropTypes.func,
    error: PropTypes.oneOfType([PropTypes.array, PropTypes.string]),
    initialValues: PropTypes.object,
    onSubmit: PropTypes.func
  }

  handleCancel = () => {
    const { history } = this.props
    history.push(ROUTES.OPERATORS)
  }

  render() {
    const { error, handleSubmit, initialValues } = this.props
    const usernameDisabled = Boolean(get(initialValues, 'email'))
    return (
      <form onSubmit={handleSubmit}>
        {error && <Alert message={error} type="error" />}
        <Field
          name="name"
          label="Name"
          htmlType="text"
          id="operatorName"
          validate={[validate.nameIsRequired]}
          component={TextField}
        />
        <Field
          name="username"
          label="Username"
          htmlType="text"
          id="operatorUsername"
          component={TextField}
          validate={usernameDisabled ? undefined : [validate.usernameIsRequired]}
          disabled={usernameDisabled}
        />
        <Field
          name="email"
          label="Email Address"
          htmlType="email"
          id="operatorEmail"
          component={TextField}
          validate={[validate.emailIsRequired]}
        />
        <Field
          name="inactive"
          label="Deactivate Operator"
          component={SwitchField}
        />
        <SubmitRow>
          <Row type="flex" gutter={20}>
            <Col>
              <Button htmlType="submit" type="primary" wide>Save</Button>
            </Col>
            <Col>
              <Button wide onClick={this.handleCancel}>Cancel</Button>
            </Col>
          </Row>
        </SubmitRow>
      </form>
    )
  }
}

export default compose(
  withRouter,
  reduxForm({
    form: 'changePassword',
    enableReinitialize: true
  })
)(OperatorForm)
