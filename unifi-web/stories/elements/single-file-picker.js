/*
  eslint-disable
  jsx-a11y/accessible-emoji,
  import/no-extraneous-dependencies,
  import/first,
  no-unused-vars
*/
import React, { Component } from 'react'

import { storiesOf } from '@storybook/react'
import { action } from '@storybook/addon-actions'
import { linkTo } from '@storybook/addon-links'

import { Aux, SingleFilePicker } from '../../src/elements'

storiesOf('Elements/SingleFilePicker', module)
  .add('Simple (no change)', () => (
    <SingleFilePicker onChange={action('onChange')} />
  ))
  .add('Controlled', () => {
    class ControlledSingleFilePicker extends Component {
      state = {
        file: '',
        data: null,
      }

      handleChange = ({ file, data }) => {
        this.setState({
          file,
          data,
        })
      }


      render() {
        const { file, data } = this.state
        return (
          <SingleFilePicker
            buttonLabel={file ? file.name : undefined}
            onChange={this.handleChange}
          />
        )
      }
    }

    return <ControlledSingleFilePicker />
  })
