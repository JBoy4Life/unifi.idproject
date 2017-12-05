import React from 'react'
import { TextInput, FormItem } from '../../elements'

const TextField = ({
  input, id, className, placeholder, label, htmlType = 'text',
  meta: { touched, error },
}) => {
  const hasErorr = touched && error
  const validationsProps = {}

  if (hasErorr) {
    validationsProps.validateStatus = 'error'
    validationsProps.help = error
    // validationsProps.hasFeedback = true
  }

  return (
    <FormItem
      label={label}
      id={id}
      {...validationsProps}
    >
      <TextInput
        type={htmlType}
        id={id}
        {...input}
        className={className}
        placeholder={placeholder}
      />
    </FormItem>
  )
}

export default TextField
