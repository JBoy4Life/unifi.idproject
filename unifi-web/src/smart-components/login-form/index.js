import React, { Component } from 'react'
import { Field, reduxForm } from 'redux-form'
import { TextInput } from '../../elements'

const FormInputText = ({
  input, id, className, placeholder, alternate,
}) => (
  <TextInput
    type="text"
    id={id}
    alternate={alternate}
    {...input}
    className={className}
    placeholder={placeholder}
  />
)

class LoginForm extends Component {
  render() {
    return (
      <div>
        <Field
          name="name"
          component={FormInputText}
        />
      </div>
    )
  }
}

export default reduxForm({
  form: 'login',
})(LoginForm)
