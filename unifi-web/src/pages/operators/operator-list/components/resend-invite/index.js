import React, { Component, Fragment } from 'react'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { Link } from 'react-router-dom'
import { API_PENDING, API_SUCCESS, API_FAIL } from 'redux/api/request'

import formSubmit from 'utils/form'
import { requestPasswordReset } from 'redux/user/actions'
import { Spinner } from 'elements'
import { withClientId } from 'hocs'
import './index.scss'

class ResendInvite extends Component {
  static propTypes = {
    clientId: PropTypes.string.isRequired,
    requestPasswordReset: PropTypes.func.isRequired,
    row: PropTypes.object.isRequired
  };

  constructor(props) {
    super(props)
    this.state = {
      status: 'INIT'
    }
  }

  componentDidMount() {
    this.mounted = true
  }

  componentWillUnmount() {
    this.mounted = false
  }

  handleResendInviteClick = (event) => {
    const { clientId, requestPasswordReset, row } = this.props
    event.preventDefault()
    this.setState({ status: API_PENDING })
    return formSubmit(requestPasswordReset, { clientId, username: row.username })
      .then(() => this.mounted && this.setState({ status: API_SUCCESS }))
      .catch(ex => this.mounted && this.setState({ status: API_FAIL }))
  };

  render() {
    const { status } = this.state
    return (
      <Fragment>
        {'INIT' === status && <Link to="/" onClick={this.handleResendInviteClick}>Resend invite</Link>}
        {API_PENDING === status && <div className="resend-invite-pending"><Spinner size={18} /></div>}
        {API_SUCCESS === status && <span className="text-primary">Invitation Resent!</span>}
        {API_FAIL === status && <span className="text-danger">Resending failed</span>}
      </Fragment>
    )
  }
}

const actions = {
  requestPasswordReset
}

export default compose(
  withClientId,
  connect(null, actions)
)(ResendInvite)
