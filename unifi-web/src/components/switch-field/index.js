import React from 'react'
import { Switch, FormItem } from '../../elements'

import './index.scss'

const SwitchField = ({
  input, id, className, label,
  meta: { touched, error },
  disabled,
}) => {
  const hasError = touched && error
  const validationsProps = {}

  if (hasError) {
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
        checked={typeof input.checked === 'undefined' ? Boolean(input.value) : input.checked}
        disabled={disabled}
        className={className}
      />
      <span className="switch-field-label">{label}</span>
    </FormItem>
  )
}

export default SwitchField
