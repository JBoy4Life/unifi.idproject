import React, { Component } from 'react'

export default (WrappedComponent) => {
  const clientId = window.location.hostname.split(".")[0];
  return (props) => (
    <WrappedComponent {...props} clientId={clientId} />
  )
}
