import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Field, reduxForm } from 'redux-form'
import { TextField } from '../../components'
import { Button } from '../../elements'

import validate from './validate'

class ClientRegisterForm extends Component {
  static propTypes = {
    onSubmit: PropTypes.func,
  }

  render() {
    const { isSubmiting, handleSubmit, onSubmit } = this.props

    return (
      <form onSubmit={handleSubmit(onSubmit)}>
        <Field
          name="displayName"
          label="Client Name"
          id="display_name"
          disabled={isSubmiting}
          component={TextField}
        />
        <Field
          name="logo"
          label="Logo (just a string for now)"
          id="logo"
          disabled={isSubmiting}
          component={TextField}
        />
        <Button loading={isSubmiting} htmlType="submit" type="primary">Create</Button>
      </form>
    )
  }
}

export default reduxForm({
  form: 'client-register',
  validate,
})(ClientRegisterForm)
