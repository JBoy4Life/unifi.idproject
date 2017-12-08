import React from 'react'
import { Select, FormItem } from '../../elements'

const getOptionProps = (option) => {
  if (typeof option === 'object') {
    return {
      label: option.label,
      value: option.value,
    }
  }

  return {
    label: option,
    value: option,
  }
}

const SelectField = ({
  input, id, className, label,
  meta: { touched, error },
  options,
}) => {
  const hasErorr = touched && error
  const validationsProps = {}

  if (hasErorr) {
    validationsProps.validationSttus = 'error'
    validationsProps.help = error
  }

  return (
    <FormItem
      label={label}
    >
      <Select
        id={id}
        {...input}
        className={className}
      >
        {options.map((option) => {
          const { value, label: text } = getOptionProps(option)
          return (
            <Select.Option key={value} value={value}>{text}</Select.Option>
        )
      })}
      </Select>
    </FormItem>
  )
}

export default SelectField
