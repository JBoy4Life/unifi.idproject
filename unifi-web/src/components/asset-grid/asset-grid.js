import React from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'
import fp from 'lodash/fp'
import moment from 'moment'

import './asset-grid.scss'

const COMPONENT_CSS_CLASSNAME = 'asset-grid'
const bemM = (suffix) => `${COMPONENT_CSS_CLASSNAME}--${suffix}`

const AssetGrid = ({ children, viewMode }) => (
  <div className={cx(COMPONENT_CSS_CLASSNAME, bemM(viewMode))}>
    {children}
  </div>
)

AssetGrid.propTypes = {
  children: PropTypes.node,
  viewMode: PropTypes.string
}

export default AssetGrid
