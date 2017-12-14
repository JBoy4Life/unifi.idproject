import React from 'react'
import { SingleFilePicker, FormItem } from '../../elements'

const SingleFilePickerField = ({
  input, id, className, label,
  meta: { touched, error },
  disabled,
}) => {
  const hasErorr = touched && error
  const validationsProps = {}

  if (hasErorr) {
    validationsProps.validateStatus = 'error'
    validationsProps.help = error
    // validationsProps.hasFeedback = true
  }

  const filename = input.value ? input.value.file.name : undefined

  return (
    <FormItem
      label={label}
      id={id}
      {...validationsProps}
    >
      <SingleFilePicker
        id={id}
        {...input}
        buttonLabel={filename}
        className={className}
        disabled={disabled}
      />
    </FormItem>
  )
}

export default SingleFilePickerField
