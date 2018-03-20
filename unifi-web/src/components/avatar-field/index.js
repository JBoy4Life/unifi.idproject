import React, { Component, Fragment } from 'react'
import cx from 'classnames'
import PropTypes from 'prop-types'

import { Avatar, FormItem, Icon } from 'elements'
import './index.scss';

const COMPONENT_CSS_CLASS = 'avatar-field'
const bemE = (suffix) => `${COMPONENT_CSS_CLASS}__${suffix}`

class AvatarField extends Component {
  static propTypes = {
    className: PropTypes.string,
    disabled: PropTypes.bool,
    htmlId: PropTypes.object,
    input: PropTypes.object,
    label: PropTypes.string,
    meta: PropTypes.object
  };

  handleOpen = (event) => {
    this.fileInputEl.value = null
    this.fileInputEl.click()
  };

  handleDelete = (event) => {
    this.fileInputEl.value = null
    this.props.input.onBlur(null)
  };

  setValue(file) {
    const { input } = this.props
    const reader = new FileReader()
    reader.readAsArrayBuffer(file)
    reader.onload = () => {
      input.onChange({
        mimeType: file.type,
        data: reader.result
      })
    }
  }

  handleChange = (event) => {
    const { input } = this.props
    const file = event.target.files[0]
    this.setValue(file)
  };

  handleSetRefs = (ref) => {
    this.fileInputEl = ref
  };

  render() {
    const { input, className, label, htmlId, meta: { touched, error }, disabled } = this.props
    const hasError = touched && error
    const validationsProps = hasError ? {
      validateStatus: 'error',
      help: error
    } : {}

    return (
      <FormItem
        label={label}
        id={htmlId}
        className={cx(COMPONENT_CSS_CLASS, className)}
        {...validationsProps}
      >
        <div className={bemE('avatar')}>
          <Avatar image={input.value} />
          <input
            type="file"
            ref={this.handleSetRefs}
            onChange={this.handleChange}
            className={bemE('file')}
            accept="image/png, image/jpeg, image/svg+xml"
          />
          <div className={bemE('overlay')}>
            <div className={bemE('actions')}>
              {input.value ? (
                <Fragment>
                  <button className={bemE('action')} type="button" onClick={this.handleOpen}>
                    <Icon type="plus-circle-o" /> Replace photo
                  </button>
                  <button className={bemE('action')} type="button" onClick={this.handleDelete}>
                    <Icon type="minus-circle-o" /> Delete photo
                  </button>
                </Fragment>
              ) : (
                <button className={bemE('action')} type="button" onClick={this.handleOpen}>
                  <Icon type="plus-circle-o" /> Add photo
                </button>
              )}
            </div>
          </div>
        </div>
      </FormItem>
    )
  }
}

export default AvatarField
