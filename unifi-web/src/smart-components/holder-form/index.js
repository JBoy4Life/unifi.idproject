import React, { Component } from 'react'
import get from 'lodash/get'
import PropTypes from 'prop-types'
import { compose } from 'redux'
import { Field, reduxForm } from 'redux-form'
import { withRouter } from 'react-router'

import * as ROUTES from 'utils/routes' 
import * as validate from './validate'
import { Alert, Button, Col, Row, SubmitRow } from 'elements'
import { AvatarField, SwitchField, TextField } from 'components'
import './index.scss'

const COMPONENT_CSS_CLASSNAME = 'holder-form'
const bemE = (suffix) => `${COMPONENT_CSS_CLASSNAME}__${suffix}`

class HolderForm extends Component {
  static propTypes = {
    handleSubmit: PropTypes.func,
    error: PropTypes.oneOfType([PropTypes.array, PropTypes.string]),
    initialValues: PropTypes.object,
    onSubmit: PropTypes.func
  }

  handleCancel = () => {
    const { history } = this.props
    const clientReference = get(initialValues, 'clientReference')
    history.push(
      clientReference
      ? ROUTES.DIRECTORY_HOLDER_DETAIL.replace(':clientReference', clientReference)
      : ROUTES.DIRECTORY
    )
  }

  render() {
    const { error, handleSubmit, initialValues } = this.props
    const editMode = Boolean(get(initialValues, 'clientReference'))

    return (
      <form onSubmit={handleSubmit} className={COMPONENT_CSS_CLASSNAME}>
        {error && <Alert message={error} type="error" className={bemE('error')} />}
        <Field
          name="name"
          label="Name"
          htmlType="text"
          validate={[validate.nameIsRequired]}
          component={TextField}
        />
        <Row>
          <Col xs={12}>
            <Field
              name="image"
              label="Photo (optional)"
              component={AvatarField}
            />
          </Col>
        </Row>
        {editMode && (
          <Field
            name="inactive"
            label="Deactivate contact"
            component={SwitchField}
          />
        )}
        <SubmitRow>
          <Row type="flex" gutter={20}>
            <Col>
              <Button htmlType="submit" type="primary" wide>
                Save
              </Button>
            </Col>
            <Col>
              <Button wide onClick={this.handleCancel}>Cancel</Button>
            </Col>
          </Row>
        </SubmitRow>
      </form>
    )
  }
}

export default compose(
  withRouter,
  reduxForm({
    form: 'holderForm',
    enableReinitialize: true
  })
)(HolderForm)
