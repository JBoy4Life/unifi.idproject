import React, { Component } from 'react'
import pick from 'lodash/pick'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link } from 'react-router-dom'

import * as ROUTES from 'config/routes'
import { Col, Row } from 'elements'
import { detectablesListSelector } from 'redux/modules/detectable/selectors'
import { formSubmit } from 'utils/form'
import { getHolder, addHolder } from 'redux/modules/holder/actions'
import { getImageData } from '../helpers'
import { holderDetailsSelector } from 'redux/modules/holder/selectors'
import { HolderForm } from 'smart-components'
import { listDetectables } from 'redux/modules/detectable/actions'
import { PageContentTitle } from 'components'
import { withClientId } from 'hocs'
import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'directory-new'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`

class ContactNew extends Component {
  static propTypes = {
    addHolder: PropTypes.func.isRequired,
    clientId: PropTypes.string.isRequired,
    history: PropTypes.object.isRequired,
    listDetectables: PropTypes.func.isRequired,
    match: PropTypes.object.isRequired
  };

  handleSubmit = (values) => {
    const { clientId, history, addHolder, match: { params: { clientReference } } } = this.props
    return formSubmit(addHolder, {
      clientId,
      clientReference: values.clientReference,
      name: values.name,
      image: values.image ? getImageData(values.image.data) : null,
      active: !values.inactive
    }).then(() => history.push(ROUTES.DIRECTORY))
  };

  render() {
    return (
      <Row>
        <Col xs={24} md={10}>
          <p className={bemE('back')}><Link to={ROUTES.DIRECTORY}>&laquo; Back</Link></p>
          <PageContentTitle>Add Contact</PageContentTitle>
          <HolderForm onSubmit={this.handleSubmit} />
        </Col>
      </Row>
    )
  }
}

export const selector = createStructuredSelector({
  detectablesList: detectablesListSelector
})

export const actions = {
  addHolder,
  listDetectables
}

export default compose(
  withClientId,
  connect(selector, actions)
)(ContactNew)
