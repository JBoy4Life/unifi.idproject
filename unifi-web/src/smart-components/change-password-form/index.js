import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { Field, reduxForm } from 'redux-form'
import { withRouter } from 'react-router'

import { TextField /* CheckboxField */ } from 'components'
import { Alert, Button, Col, Row, SubmitRow } from 'elements'
            
import validate from './validate'

class ChangePasswordForm extends Component {
  static propTypes = {
    onSubmit: PropTypes.func,
  }

  handleCancel = () => {
    const { history } = this.props
    history.push('/')
  }

  render() {
    const { error, handleSubmit } = this.props
    return (
      <form onSubmit={handleSubmit}>
        {error && <Alert message={error} type="error" />}
        <Field
          name="currentPassword"
          label="Current Password"
          htmlType="password"
          id="currentPassword"
          component={TextField}
        />
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
        <SubmitRow>
          <Row type="flex" gutter={20}>
            <Col>
              <Button htmlType="submit" type="primary" wide>Submit</Button>
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
    validate,
  })
)(ChangePasswordForm)
