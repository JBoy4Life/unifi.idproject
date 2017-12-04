// you can use this file to add your custom webpack plugins, loaders and anything you like.
// This is just the basic way to add additional webpack configurations.
// For more information refer the docs: https://storybook.js.org/configurations/custom-webpack-config

// IMPORTANT
// When you add this file, we won't add the default configurations which is similar
// to "React Create App". This only has babel loader to load JavaScript.
const webpackConfig = require('../config/webpack.config.dev')
//
// module.exports = {
//   plugins: [],
//   module: {
//     rules: webpackConfig.module.rules
//   },
// };

const merge = require('webpack-merge')

// const sharedWebpackConfig = require('./../config/webpack/shared');

module.exports = (storybookBaseConfig, configType) => {

  if (configType === 'PRODUCTION') {
    // Removing uglification until we figure out a fix for that.
    storybookBaseConfig.plugins.pop();
  }


  const extension = {
    resolve: webpackConfig.resolve,
    module: {
      rules: webpackConfig.module.rules
    },
  };

  return merge(storybookBaseConfig,extension);
};
