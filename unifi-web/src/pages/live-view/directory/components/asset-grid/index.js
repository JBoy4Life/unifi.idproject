import React from 'react'
import moment from 'moment'

import './index.scss'

const formatTime = (value) => {
  const m = moment(value)
  return `${m.format('HH:MM:ss')}`
}

const renderAssetCard = item => (
  <div className="asset-card" key={item.clientReference}>
    <div className="asset-card-image" />
    <div className="asset-meta">
      <div className="asset-card-name">{item.client.name}</div>
      <div className="asset-card-name">ID {item.clientReference}</div>
      <div className="asset-card-name">{formatTime(item.timestamp)}</div>
      <div className="asset-card-name">{item.zone.name}</div>
    </div>
  </div>
)

const AssetGrid = props => (
  <div className="asset-grid">
    {props.items.map(renderAssetCard)}
  </div>
)

export default AssetGrid

