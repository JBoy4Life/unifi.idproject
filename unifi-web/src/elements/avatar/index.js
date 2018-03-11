import React from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'

import defaultAvatar from './default-avatar.svg'
import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'avatar'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`

const Avatar = ({ image, className }) => (
  <div className={cx(COMPONENT_CSS_CLASSNAME, className)}>
    <div className={bemE('image')}
      style={{
        backgroundImage: image
        ? `url(data:${image.type};base64,${image.data})`
        : `url(${defaultAvatar})`
      }}
    />
  </div>
)

Avatar.propTypes = {
  className: PropTypes.string,
  image: PropTypes.object
}

export default Avatar
