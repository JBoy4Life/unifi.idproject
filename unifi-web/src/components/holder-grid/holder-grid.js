import React from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'
import fp from 'lodash/fp'
import moment from 'moment'

import './holder-grid.scss'

const COMPONENT_CSS_CLASSNAME = 'holder-grid'
const bemM = (suffix) => `${COMPONENT_CSS_CLASSNAME}--${suffix}`

const HolderGrid = ({ children, viewMode }) => (
  <div className={cx(COMPONENT_CSS_CLASSNAME, bemM(viewMode))}>
    {children}
  </div>
)

HolderGrid.propTypes = {
  children: PropTypes.node,
  viewMode: PropTypes.string
}

export default HolderGrid
