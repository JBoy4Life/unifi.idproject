import React, { Component } from 'react'
import fp from 'lodash/fp'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link } from 'react-router-dom'

import * as ROUTES from 'config/routes'
import { Avatar, Col, Row } from 'elements'
import { detectablesListSelector } from 'redux/modules/detectable/selectors'
import { getHolder } from 'redux/modules/holder/actions'
import { holderDetailsSelector } from 'redux/modules/holder/selectors'
import { listDetectables } from 'redux/modules/detectable/actions'
import { PageContentTitle } from 'components'
import { withClientId } from 'hocs'
import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'directory-details'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`

class ContactDetails extends Component {
  componentDidMount() {
    const { clientId, getHolder, listDetectables, match } = this.props
    const { clientReference } = match.params
    getHolder({ clientId, clientReference, with: ['image', 'detectable-type'] })
    listDetectables({ clientId, assignment: clientReference })
  }

  render() {
    const { holder, detectablesList, match } = this.props
    const { clientReference } = match.params
    const detectables = fp.filter({ assignment: clientReference })(detectablesList)
    const editUrl = ROUTES.DIRECTORY_HOLDER_EDIT.replace(':clientReference', clientReference)

    return holder ? (
      <Row>
        <Col xs={24} md={8}>
          <p className={bemE('back')}><Link to={ROUTES.DIRECTORY}>&laquo; Back</Link></p>
          <PageContentTitle>
            <Link to={editUrl} className={bemE('edit-link')}>
              <svg viewBox="0 0 14 16" version="1.1" width="36" height="36">
                <path fill-rule="evenodd" fill="currentColor" d="M0 12v3h3l8-8-3-3-8 8zm3 2H1v-2h1v1h1v1zm10.3-9.3L12 6 9 3l1.3-1.3a.996.996 0 0 1 1.41 0l1.59 1.59c.39.39.39 1.02 0 1.41z" />
              </svg>
            </Link>
            {holder.name}
          </PageContentTitle>
          <p>ID number: {holder.clientReference}</p>
          <Avatar image={holder.image} className={bemE('avatar')} />
          <h3 className={bemE('dt-title')}>Detectables</h3>
          {detectables.map(item => (
            <div className={bemE('dt-item')} key={item.detectableId}>
              <div className={bemE('dt-type')}>{item.detectableType}: {item.detectableId}</div>
              <div>{item.description}</div>
            </div>
          ))}
        </Col>
      </Row>
    ) : null
  }
}

export const selector = createStructuredSelector({
  detectablesList: detectablesListSelector,
  holder: holderDetailsSelector
})

export const actions = {
  getHolder,
  listDetectables
}

export default compose(
  withClientId,
  connect(selector, actions),
)(ContactDetails)
