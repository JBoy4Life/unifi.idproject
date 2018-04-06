import React, { Component } from 'react'
import get from 'lodash/get'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { Link, withRouter } from 'react-router-dom'

import * as ROUTES from 'config/routes'
import { Col, Row } from 'elements'
import { formSubmit } from 'utils/form'
import { getOperator, registerOperator } from 'redux/modules/model/operator'
import { operatorDetailsSelector } from 'redux/selectors'
import { OperatorForm } from 'smart-components'
import { PageContentTitle } from 'components'
import { withClientId } from 'hocs'
import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'operator-invite'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`

class OperatorInvite extends Component {
  static propTypes = {
    clientId: PropTypes.string.isRequired,
    history: PropTypes.object.isRequired,
    registerOperator: PropTypes.func.isRequired
  };

  handleSubmit = (values) => {
    const { clientId, history, registerOperator } = this.props
    return formSubmit(registerOperator, { clientId, ...values })
      .then(() => history.push(ROUTES.OPERATORS))
  };

  render() {
    return (
      <Row>
        <Col xs={24} sm={12} md={10}>
          <p className={bemE('back')}><Link to={ROUTES.OPERATORS}>&laquo; Back</Link></p>
          <PageContentTitle>Invite a new operator</PageContentTitle>
          <p className={bemE('text')}>
            Add a new operator's details and they will be sent an invitation to join your company's unifi.id account.
          </p>
          <OperatorForm onSubmit={this.handleSubmit} />
        </Col>
      </Row>
    )
  }
}

export const actions = {
  registerOperator
}

export default compose(
  withRouter,
  withClientId,
  connect(null, actions),
)(OperatorInvite)
