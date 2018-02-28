import React, { Component } from 'react'
import cx from 'classnames'
import PropTypes from 'prop-types'
import MdCropDin from 'react-icons/lib/md/crop-din'
import MdFormatListBulleted from 'react-icons/lib/md/format-list-bulleted'
import TiThLargeOutline from 'react-icons/lib/ti/th-large-outline'
import { Col, Row } from 'elements'
import { PageContentUnderTitle } from 'components'
import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'directory-view-header'
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
    viewMode: PropTypes.string
  }

  handleViewModeChange = (mode) => () => {
    this.props.onViewModeChange(mode)
  }

  render() {
    const { onViewModeChange, resultCount, viewMode } = this.props
    return (
      <div className={COMPONENT_CSS_CLASSNAME}>
        <Row gutter={16}>
          <Col xs={12}>
            {resultCount > 0 && <PageContentUnderTitle>Showing {resultCount} contacts</PageContentUnderTitle>}
          </Col>
          <Col xs={12} className={bemE('mode')}>
            <ToolButton selected={viewMode === 'list'} onClick={this.handleViewModeChange('list')}>
              <MdFormatListBulleted />
            </ToolButton>
            <ToolButton selected={viewMode === 'large-tile'} onClick={this.handleViewModeChange('large-tile')}>
              <MdCropDin />
            </ToolButton>
            <ToolButton selected={viewMode === 'small-tile'} onClick={this.handleViewModeChange('small-tile')}>
              <TiThLargeOutline />
            </ToolButton>
          </Col>
        </Row>
      </div>
    )
  }
} 

export default ViewModeHeader
