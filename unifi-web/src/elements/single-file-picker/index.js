import React, { Component } from 'react'
import Icon from '../icon'
import Button from '../button'

import './index.scss'

export default class SingleFilePicker extends Component {
  static defaultProps = {
    buttonLabel: 'Pick file',
    value: {
      file: '',
    },
  }

  setInputRef = (el) => {
    if (el) {
      this.input = el
    }
  }

  handleFilePicked = (event) => {
    // event.preventDefault()
    const { onChange, onError } = this.props

    const file = event.target.files[0]

    const reader = new FileReader()

    reader.addEventListener('load', () => {
      onChange({
        file, data: reader.result,
      })
    }, false)

    reader.addEventListener('error', (error) => {
      onError(error)
    }, false)

    if (file) {
      reader.readAsDataURL(file)
    }
  }

  handleButtonClick = () => {
    this.input.click()
  }

  render() {
    const { buttonLabel } = this.props

    return (
      <div
        className="single-file-picker"
      >
        <Button
          onClick={this.handleButtonClick}
        >
          <Icon type="upload" />
          {buttonLabel}
        </Button>
        <input
          ref={this.setInputRef}
          className="single-file-picker-input"
          type="file"
          accept=""
          onChange={this.handleFilePicked}
        />
      </div>
    )
  }
}
