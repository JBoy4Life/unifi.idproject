import React from 'react'
import { TextInput, FormItem } from '../../elements'

const TextField = ({
  input, id, className, placeholder, label, htmlType = 'text',
  meta: { touched, error },
  disabled, extra
}) => {
  const hasError = touched && error
  const validationsProps = {}

  if (hasError) {
    validationsProps.validateStatus = 'error'
    validationsProps.help = error
    // validationsProps.hasFeedback = true
  }

  return (
    <FormItem
      label={label}
      id={id}
      extra={extra}
      {...validationsProps}
    >
      <TextInput
        type={htmlType}
        id={id}
        {...input}
        className={className}
        placeholder={placeholder}
        disabled={disabled}
      />
    </FormItem>
  )
}

export default TextField
