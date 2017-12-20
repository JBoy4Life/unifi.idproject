import React from 'react'

import './index.scss'

const renderAssetCard = item => (
  <div className="asset-card">
    <div className="asset-card-image" />
    <div className="asset-meta">
      <div className="asset-card-name">{item.name}</div>
      <div className="asset-card-name">ID {item.id}</div>
    </div>
  </div>
)

const AssetGrid = props => (
  <div className="asset-grid">
    {props.items.map(renderAssetCard)}
  </div>
)

export default AssetGrid

