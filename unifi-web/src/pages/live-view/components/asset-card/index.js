import React from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'
import moment from 'moment'

import defaultAvatar from './default-avatar.png'
import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'asset-card'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`
const bemM = (suffix) => `${COMPONENT_CSS_CLASSNAME}--${suffix}`

const formatTime = (value) => moment.utc(value).format('LTS')

const AssetCard = ({ item, viewMode }) => (
  <div
    className={cx(COMPONENT_CSS_CLASSNAME, bemM(viewMode))}
  >
    <div
      className={bemE('image')}
      style={{
        backgroundImage: item.client.image
        ? `url(data:${item.client.image.type};base64,${item.client.image.data})`
        : `url(${defaultAvatar})`
      }}
    />
    <div className={bemE('meta')}>
      <div className={bemE('field')}>{item.client.name}</div>
      <div className={bemE('field')}>ID {item.clientReference}</div>
      <div className={bemE('field')}>{formatTime(item.detectionTime)}</div>
    </div>
  </div>
)

AssetCard.propTypes = {
  item: PropTypes.object,
  viewMode: PropTypes.string
}

export default AssetCard
