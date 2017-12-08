import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Field, reduxForm } from 'redux-form'
import {
  TextField, CheckboxField, SwitchField, SelectField,
} from '../../components'
import { Button, Row, Col } from '../../elements'

import validate from './validate'

class NotificaitonForm extends Component {
  static propTypes = {
    onSubmit: PropTypes.func,
  }

  render() {
    const { handleSubmit, onSubmit } = this.props

    return (
      <form onSubmit={handleSubmit(onSubmit)}>
        <h2>Componnet demo only</h2>
        <p>This form is just a showcase of the form elements</p>
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
        <Field
          name="rememberme"
          label="Remember me"
          id="remember"
          component={CheckboxField}
        />
        <Row gutter={24}>
          <Col span={12}>
            <Field
              name="zone1"
              label="zone one"
              id="zone1"
              component={SwitchField}
            />
          </Col>

          <Col span={12}>
            <Field
              name="zone2"
              label="zone two"
              id="zone2"
              component={SwitchField}
            />
          </Col>

          <Col span={12}>
            <Field
              name="zone3"
              label="zone three"
              id="zone3"
              component={SwitchField}
            />
          </Col>

          <Col span={12}>
            <Field
              name="zone4"
              label="zone four"
              id="zone4"
              component={SwitchField}
            />
          </Col>

        </Row>
        <Field
          name="options"
          label="Options"
          id="options"
          component={SelectField}
          options={['one', 'two', 'something else']}
        />
        <Button htmlType="submit" type="primary">Save</Button>
      </form>
    )
  }
}

export default reduxForm({
  form: 'notification',
  validate,
})(NotificaitonForm)
