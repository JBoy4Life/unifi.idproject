import React from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'

import defaultAvatar from './default-avatar.png'
import './asset-card.scss'

const COMPONENT_CSS_CLASSNAME = 'asset-card'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`

const AssetCard = ({ image, children }) => (
  <div
    className={cx(COMPONENT_CSS_CLASSNAME)}
  >
    <div
      className={bemE('image')}
      style={{
        backgroundImage: image
        ? `url(data:${image.type};base64,${image.data})`
        : `url(${defaultAvatar})`
      }}
    />
    <div className={bemE('text')}>
      {children}
    </div>
  </div>
)

const Field = ({ children }) => (
  <div className={bemE('field')}>{children}</div>
)

AssetCard.Field = Field

AssetCard.propTypes = {
  children: PropTypes.node,
  image: PropTypes.object
}

export default AssetCard
