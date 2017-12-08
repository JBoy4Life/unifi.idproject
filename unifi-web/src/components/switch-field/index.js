import React from 'react'
import { Switch, FormItem } from '../../elements'

import './index.scss'

const SwitchField = ({
  input, id, className, label,
  meta: { touched, error },
}) => {
  const hasErorr = touched && error
  const validationsProps = {}

  if (hasErorr) {
    validationsProps.validationSttus = 'error'
    validationsProps.help = error
  }

  return (
    <FormItem
      id={id}
      {...validationsProps}
    >
      <Switch
        id={id}
        {...input}
        className={className}
      />
      <span className="switch-field-label">{label}</span>
    </FormItem>
  )
}

export default SwitchField
