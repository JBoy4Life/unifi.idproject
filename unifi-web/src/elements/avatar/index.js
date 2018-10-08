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

const Avatar = ({ image, className }) => {
  let element
  return (
    <div className={cx(COMPONENT_CSS_CLASSNAME, className)}>
      <img className={bemE('image')}
        src={image && typeof image === 'string'
            ? image
            : `data:${image.mimeType};base64,${toBase64(image.data)}` }
        onError={() => ((src) => {element.src = src})(defaultAvatar)}
        ref={(ref) => {element = ref}} />
    </div>
  )
}

Avatar.propTypes = {
  className: PropTypes.string,
  image: PropTypes.oneOfType([PropTypes.object, PropTypes.string])
}

export default Avatar
