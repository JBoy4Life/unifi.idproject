import React from 'react'
import fp from 'lodash/fp'
import moment from 'moment'

import './index.scss'

const formatTime = (value) => {
  const m = moment(value)
  return `${m.format('HH:MM:ss')}`
}

const renderAssetCard = item => (
  <div className="asset-card" key={item.clientReference}>
    <div
      className="asset-card__image"
      style={{ backgroundImage: `url(data:image/png;base64,${item.client.image})` }}
    />
    <div className="asset-card__meta">
      <div className="asset-card__field">{item.client.name}</div>
      <div className="asset-card__field">ID {item.clientReference}</div>
      <div className="asset-card__field">{formatTime(item.timestamp)}</div>
      <div className="asset-card__field">{item.zone.name}</div>
    </div>
  </div>
)

const AssetGrid = props => (
  <div className="asset-grid">
    {fp.compose(
      fp.map(renderAssetCard),
      fp.sortBy('timestamp')
    )(props.items)}
  </div>
)

export default AssetGrid

