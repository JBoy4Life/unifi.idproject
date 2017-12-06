/*
  eslint-disable
  jsx-a11y/accessible-emoji,
  import/no-extraneous-dependencies,
  import/first,
  no-unused-vars
*/
import React from 'react'

import { storiesOf } from '@storybook/react'
import { action } from '@storybook/addon-actions'
import { linkTo } from '@storybook/addon-links'

import { Aux, Row, Col } from '../../src/elements'
import { PageContent } from '../../src/components'

import './grid.scss'

storiesOf('Components/Page Content', module)
  .add('without sidebar', () => (
    <PageContent>
      <PageContent.Main>
      Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed nec justo eu
      dui condimentum ullamcorper. Proin velit dolor, rutrum ut justo at, fringilla
      consequat elit. Integer sed eros ultrices, tristique turpis quis, tincidunt
      massa. Fusce vitae sem enim. In hac habitasse platea dictumst. Pellentesque
      habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas.
      Suspendisse dui nisi, feugiat quis dapibus et, interdum a nunc. Ut tempor nisl
      at risus vestibulum, et pharetra lectus tincidunt. Curabitur feugiat tincidunt
      ullamcorper.
      </PageContent.Main>
    </PageContent>
  ))
  .add('with sidebar', () => (
    <PageContent>
      <PageContent.Sidebar>
      Phasellus dolor nunc, pretium nec diam in, scelerisque gravida elit. Fusce et
      tempor lorem. Maecenas et risus enim. Pellentesque eget laoreet lorem.
      Pellentesque ornare mi id arcu condimentum convallis. Suspendisse porta dui at
      varius ornare. In quis quam massa. Suspendisse a vestibulum ante, vehicula dictum
      sem. Fusce et porttitor augue. Nullam eleifend, odio non dignissim blandit, velit
      ex consectetur lorem, a auctor nibh quam id orci.
      </PageContent.Sidebar>
      <PageContent.Main>
      Proin feugiat dui eget dolor vehicula, et tristique est convallis. Nam feugiat,
      enim sed porta congue, nisl erat malesuada justo, id pellentesque dui tortor vel
      nunc. Cras dui nunc, rhoncus sit amet augue a, bibendum maximus enim. Suspendisse
      elementum orci eget dui fermentum, sed dictum lacus vulputate. Ut libero risus,
      malesuada vitae dui vel, hendrerit commodo lacus. Sed sit amet est eu ligula tempus
      interdum ultrices sed lorem. Fusce a purus id metus cursus pretium. Curabitur ornare
      massa turpis, id lacinia elit vehicula vitae. Donec hendrerit, elit a rhoncus
      feugiat, elit erat interdum purus, sit amet congue lorem dui vel neque. Pellentesque
      et porta lacus. Nam erat elit, vestibulum id dui eu, ultrices dapibus velit.
      </PageContent.Main>
    </PageContent>
  ))

