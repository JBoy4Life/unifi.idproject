import React from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'
import fp from 'lodash/fp'
import moment from 'moment'

import './index.scss'
import AssetCard from '../asset-card'

const COMPONENT_CSS_CLASSNAME = 'asset-grid'

const AssetGrid = ({ items, viewMode }) => (
  <div className={COMPONENT_CSS_CLASSNAME}>
    {fp.compose(
      fp.map(item => (
        <AssetCard
          item={item}
          key={item.clientReference}
          viewMode={viewMode}
        />
      )),
      fp.sortBy('timestamp')
    )(items)}
  </div>
)

AssetGrid.propTypes = {
  items: PropTypes.array,
  viewMode: PropTypes.string
}

export default AssetGrid
