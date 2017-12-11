import React from 'react'

import './index.scss'

const StairsIcon = () => (
  <div className="floor-view-stiars-icon">
    <div />
    <div />
    <div />
    <div />
  </div>
)

const CardStack = () => (
  <div className="flipped-card-stack">

    <div className="flipped-card-container">
      <div className="flipped-card">
        <div className="floor-layout">
          <div className="floor-section">
            <div className="floor-section-title">
            Ground Floor
            </div>
          </div>
        </div>
      </div>
    </div>

    <div className="flipped-card-container">
      <div className="flipped-card">
        <div className="floor-layout content-type-3-sections">
          <div className="floor-section">
            <div className="floor-section-title">
            S2C
            </div>
            <StairsIcon />
          </div>
          <div className="floor-section">
            <div className="floor-section-title">
            S2B
            </div>
          </div>
          <div className="floor-section">
            <div className="floor-section-title">
            S2A
            </div>
            <StairsIcon />
          </div>
        </div>
      </div>
    </div>

    <div className="flipped-card-container">
      <div className="flipped-card">
        <div className="floor-layout">
          <div className="floor-section">
            <div className="floor-section-title">
            S3A
            </div>
            <StairsIcon />
          </div>
        </div>
      </div>
    </div>

    <div className="flipped-card-container">
      <div className="flipped-card">
        <div className="floor-layout">
          <div className="floor-section">
            <div className="floor-section-title">
            Rooftop?
            </div>
            <StairsIcon />
          </div>
        </div>
      </div>
    </div>
  </div>
)

export default CardStack
