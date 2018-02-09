import React from 'react'
import fp from 'lodash/fp'
import moment from 'moment'

import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'asset-card'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`

const formatTime = (value) => {
  const m = moment(value)
  return `${m.format('HH:MM:ss')}`
}

const AssetCard = ({ item, itemsPerRow }) => (
  <div
    className={COMPONENT_CSS_CLASSNAME}
    style={{ width: `${100 / (itemsPerRow || 1)}%` }}
  >
    <div
      className={bemE('image')}
      style={{ backgroundImage: `url(data:image/png;base64,${item.client.image})` }}
    />
    <div className={bemE('meta')}>
      <div className={bemE('field')}>{item.client.name}</div>
      <div className={bemE('field')}>ID {item.clientReference}</div>
      <div className={bemE('field')}>{formatTime(item.timestamp)}</div>
      <div className={bemE('field')}>{item.zone.name}</div>
    </div>
  </div>
)

export default AssetCard
