import React from 'react'
import { Checkbox, FormItem } from '../../elements'

const CheckboxField = ({
  input, id, className, label,
  meta: { touched, error },
  disabled,
}) => {
  const hasErorr = touched && error
  const validationsProps = {}

  if (hasErorr) {
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
