import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Field, reduxForm } from 'redux-form'
import { TextField, SelectField } from '../../components'
import { Button } from '../../elements'

import validate from './validate'

class SiteWizardDetailsForm extends Component {
  static propTypes = {
    onSubmit: PropTypes.func,
  }

  render() {
    const { handleSubmit, onSubmit } = this.props

    return (
      <form onSubmit={handleSubmit(onSubmit)}>
        <Field
          name="site_name"
          label="Name of site"
          id="name"
          component={TextField}
        />
        <Field
          name="site_address"
          label="Address"
          id="address"
          component={TextField}
        />
        <Field
          name="site_floor_count"
          label="Number of floors of entire building (including ground floor)"
          id="floor_count"
          component={SelectField}
          options={['1', '2', '3', '4']}
        />
        <Field
          name="site_stairwells_count"
          label="Number of stairwells"
          id="stairwells"
          component={SelectField}
          options={['1', '2']}
        />
        <Button>Back</Button>
        <Button htmlType="submit" type="primary">Next</Button>
        <Button>Cancel</Button>
      </form>
    )
  }
}

export default reduxForm({
  form: 'site',
  validate,
})(SiteWizardDetailsForm)
