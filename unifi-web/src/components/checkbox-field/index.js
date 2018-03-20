import React from 'react'
import { Checkbox, FormItem } from '../../elements'

const CheckboxField = ({
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
    <FormItem>
      <Checkbox
        id={id}
        {...input}
        disabled={disabled}
        className={className}
      >
        {label}
      </Checkbox>
    </FormItem>
  )
}

export default CheckboxField
