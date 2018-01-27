import React from 'react'
import { DatePicker, FormItem } from 'elements'
const { RangePicker } = DatePicker

const DateRangeField = ({
  input, id, className, label, defaultValue,
  meta: { touched, error },
  disabled,
}) => {
  const hasError = touched && error
  const validationsProps = {}

  if (hasError) {
    validationsProps.validateStatus = 'error'
    validationsProps.help = error
  }

  return (
    <FormItem
      label={label}
      id={id}
      {...validationsProps}
    >
      <RangePicker
        id={id}
        disabled={disabled}
        className={className}
        format='L'
        defaultValue={defaultValue}
        {...input}
      />
    </FormItem>
  )
}

export default DateRangeField
