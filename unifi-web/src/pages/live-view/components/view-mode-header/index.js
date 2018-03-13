import React, { Component } from 'react'
import cx from 'classnames'
import PropTypes from 'prop-types'
import { Col, Icon, Row } from 'elements'
import { PageContentUnderTitle } from 'components'
import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'live-view-header'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`
const handleFilterOption = (input, option) => {
  return option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
}

const ToolButton = ({ children, selected, onClick }) => (
  <span
    tabIndex={0}
    className={cx(bemE('button'), { [bemE('button--selected')]: selected })}
    onClick={onClick}
  >
    {children}
  </span>
)

class ViewModeHeader extends Component {
  static propTypes = {
    onViewModeChange: PropTypes.func,
    resultCount: PropTypes.number,
    viewMode: PropTypes.string,
    zoneId: PropTypes.oneOfType([PropTypes.string, PropTypes.number])
  }

  handleViewModeChange = (mode) => () => {
    this.props.onViewModeChange(mode)
  }

  render() {
    const { onViewModeChange, resultCount, viewMode, zoneId } = this.props
    const zoneIsSelected = Boolean(zoneId)

    return zoneIsSelected ? (
      <div className={COMPONENT_CSS_CLASSNAME}>
        <Row gutter={16} type="flex" align="middle">
          <Col xs={12}>
            <PageContentUnderTitle>Showing {resultCount} contacts</PageContentUnderTitle>
          </Col>
          <Col xs={12} className={bemE('mode')}>
            <ToolButton selected={viewMode === 'large'} onClick={this.handleViewModeChange('large')}>
              <Icon type="laptop" />
            </ToolButton>
            <ToolButton selected={viewMode === 'small'} onClick={this.handleViewModeChange('small')}>
              <Icon type="appstore-o" />
            </ToolButton>
          </Col>
        </Row>
      </div>
    ) : null
  }
} 

export default ViewModeHeader
