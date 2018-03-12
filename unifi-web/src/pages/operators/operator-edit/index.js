import React, { Component } from 'react'
import get from 'lodash/get'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link, withRouter } from 'react-router-dom'

import * as ROUTES from 'utils/routes'
import { Col, Row } from 'elements'
import { formSubmit } from 'utils/form'
import { getOperator, updateOperator } from 'redux/operator/actions'
import { operatorDetailsSelector } from 'redux/operator/selectors'
import { OperatorForm } from 'smart-components'
import { PageContentTitle } from 'components'
import { withClientId } from 'hocs'
import './index.scss'


const COMPONENT_CSS_CLASSNAME = 'operator-edit'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`

class OperatorEdit extends Component {
  static propTypes = {
    clientId: PropTypes.string.isRequired,
    getOperator: PropTypes.func.isRequired,
    match: PropTypes.object.isRequired,
    history: PropTypes.object.isRequired,
    operator: PropTypes.object,
    updateOperator: PropTypes.func.isRequired
  };

  componentDidMount() {
    const { clientId, getOperator, match: { params: { username } } } = this.props
    getOperator({ clientId, username })
  }

  handleSubmit = (values) => {
    const { clientId, history, operator, updateOperator } = this.props
    const username = get(operator, 'username');
    const changes = {
      name: values.name,
      email: values.email,
      active: !values.inactive
    }
    return formSubmit(updateOperator, { clientId, username, changes })
      .then(() => history.push(ROUTES.OPERATORS))
  };

  render() {
    const { operator } = this.props
    const initialValues = operator ? {
      ...operator,
      inactive: !operator.active
    } : null

    return (
      <Row>
        <Col xs={24} sm={12} md={10}>
          <p className={bemE('back')}><Link to={ROUTES.OPERATORS}>&laquo; Back</Link></p>
          {operator && <PageContentTitle>Edit an operator</PageContentTitle>}
          <OperatorForm initialValues={initialValues} onSubmit={this.handleSubmit} />
        </Col>
      </Row>
    )
  }
}

export const selector = createStructuredSelector({
  operator: operatorDetailsSelector
})

export const actions = {
  getOperator,
  updateOperator
}

export default compose(
  withRouter,
  withClientId,
  connect(selector, actions),
)(OperatorEdit)
