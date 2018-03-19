import React from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'

import defaultAvatar from './default-avatar.svg'
import { base64EncodeArrayBuffer } from 'utils/helpers'
import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'avatar'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`

const toBase64 = (data) =>
  data instanceof ArrayBuffer
    ? base64EncodeArrayBuffer(data)
    : data

const Avatar = ({ image, className }) => (
  <div className={cx(COMPONENT_CSS_CLASSNAME, className)}>
    <div className={bemE('image')}
      style={{
        backgroundImage: image
        ? typeof image === 'string'
          ? `url(${image})`
          : `url(data:${image.mimeType};base64,${toBase64(image.data)})`
        : `url(${defaultAvatar})`
      }}
    />
  </div>
)

Avatar.propTypes = {
  className: PropTypes.string,
  image: PropTypes.oneOfType([PropTypes.object, PropTypes.string])
}

export default Avatar
