import React from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'

import { Avatar } from 'elements'
import './holder-card.scss'

const COMPONENT_CSS_CLASSNAME = 'holder-card'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`

const HolderCard = ({ image, children }) => (
  <div
    className={cx(COMPONENT_CSS_CLASSNAME)}
  >
    <Avatar image={image} className={bemE('image')} />
    <div className={bemE('text')}>
      {children}
    </div>
  </div>
)

const Field = ({ children }) => (
  <div className={bemE('field')}>{children}</div>
)

HolderCard.Field = Field

HolderCard.propTypes = {
  children: PropTypes.node,
  image: PropTypes.object
}

export default HolderCard
