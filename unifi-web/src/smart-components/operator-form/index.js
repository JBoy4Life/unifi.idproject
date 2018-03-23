import React, { Component } from 'react'
import get from 'lodash/get'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { Field, reduxForm } from 'redux-form'
import { withRouter } from 'react-router'

import * as ROUTES from 'config/routes' 
import * as validate from './validate'
import { Alert, Button, Col, Row, SubmitRow } from 'elements'
import { SwitchField, TextField } from 'components'
import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'operator-form'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`

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
      <form onSubmit={handleSubmit} className={COMPONENT_CSS_CLASSNAME}>
        {error && <Alert message={error} type="error" className={bemE('error')} />}
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
        {usernameDisabled && (
          <Field
            name="inactive"
            label="Deactivate Operator"
            component={SwitchField}
          />
        )}
        <SubmitRow>
          <Row type="flex" gutter={20}>
            <Col>
              <Button htmlType="submit" type="primary" wide>
                {usernameDisabled ? 'Save' : 'Send Invite'}
              </Button>
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
    form: 'operatorForm',
    enableReinitialize: true
  })
)(OperatorForm)
