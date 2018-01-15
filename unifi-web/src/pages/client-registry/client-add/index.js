import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { ClientRegisterForm } from '../../../smart-components'

import { noop } from '../../../utils/helpers'

import './index.scss'


export default class ClientAdd extends Component {
  static defaultProps = {
    createClient: noop,
  }

  static propTypes = {
    createClient: PropTypes.func,
  }

  state = {
    isSubmiting: null,
  }

  handleSubmit = (data) => {
    this.setState({
      isSubmiting: true,
    })

    this.props.createClient({
      ...data,
      // logo: data.logo.data,
      logo: ' ',
      clientId: data.displayName.replace(/ /g, '-').toLowerCase(),
    })
      .then((message) => {
        this.setState({ isSubmiting: false })
        this.props.onCreatedClient(message)
      })
      .catch(err => (console.error(err)))
  }

  render() {
    const { isSubmiting } = this.state
    // const { route: { createClient } } = this.props

    return (
      <div className="client-registry-add-container">
        <div className="form-container">
          <ClientRegisterForm isSubmiting={isSubmiting} onSubmit={this.handleSubmit} />
        </div>
      </div>
    )
  }
}
